package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.inventories.FriendManageInventory;
import de.sean.blockprot.bukkit.inventories.InventoryState;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.bukkit.nbt.StatHandler;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import de.sean.blockprot.bukkit.util.PlayerNameResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handles the {@code /blockprot friends} subcommand:
 *
 * <pre>
 * /bp friends              – opens the friend-list GUI (default-friends context)
 * /bp friends addall &lt;player&gt; – adds &lt;player&gt; to ALL protected blocks of the executor
 * </pre>
 */
public final class FriendsAddAllCommand implements CommandExecutor {

    private static final long SEARCH_MSG_TIMEOUT_TICKS = 60L;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // /bp friends  →  open GUI
        if (args.length == 1) {
            InventoryState state = new InventoryState(null);
            state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH;
            InventoryState.set(player.getUniqueId(), state);
            var inv = new FriendManageInventory().fill(player);
            if (inv != null) player.openInventory(inv);
            return true;
        }

        // /bp friends addall <player>
        if (args.length < 3 || !args[1].equalsIgnoreCase("addall")) {
            sendAction(player, Translator.get(TranslationKey.MESSAGES__FRIENDS_ADDALL_USAGE));
            return true;
        }

        final String targetName = args[2];

        // Show "Searching…" action-bar, auto-clear after 3 s
        sendAction(player, Translator.get(TranslationKey.MESSAGES__FRIENDS_SEARCHING)
            .replace("{player}", targetName));
        final BukkitTask[] clearTask = {null};
        clearTask[0] = Bukkit.getScheduler().runTaskLater(BlockProt.getInstance(),
            () -> player.sendActionBar(Component.empty()), SEARCH_MSG_TIMEOUT_TICKS);

        // 1. Resolve player UUID asynchronously (may hit Mojang API)
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), () -> {

            OfflinePlayer target = PlayerNameResolver.findOfflinePlayer(targetName);
            if (target == null) {
                // Last-resort deprecated lookup
                @SuppressWarnings("deprecation")
                OfflinePlayer fallback = Bukkit.getOfflinePlayer(targetName);
                if (fallback.hasPlayedBefore()) target = fallback;
            }

            if (clearTask[0] != null) { clearTask[0].cancel(); clearTask[0] = null; }

            if (target == null || target.getUniqueId() == null) {
                Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                    sendAction(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                    Bukkit.getScheduler().runTaskLater(BlockProt.getInstance(),
                        () -> player.sendActionBar(Component.empty()), SEARCH_MSG_TIMEOUT_TICKS);
                });
                return;
            }

            final String targetUuid = target.getUniqueId().toString();

            // 2. All NBT work MUST run on the main thread
            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                player.sendActionBar(Component.empty());

                // Collect the player's protected block locations from stats
                final PlayerBlocksStatistic stat = new PlayerBlocksStatistic();
                StatHandler.getStatistic(stat, player);

                int modified = 0;
                for (var entry : stat.get()) {
                    var loc = entry.get();
                    if (loc.getWorld() == null) continue;
                    try {
                        var block = loc.getWorld().getBlockAt(loc);
                        if (!BlockProt.getDefaultConfig().isLockable(block.getType())) continue;
                        var handler = new BlockNBTHandler(block);
                        if (!handler.isOwner(player.getUniqueId())) continue;
                        if (!handler.containsFriend(targetUuid)) {
                            handler.addFriend(targetUuid);
                            handler.applyToOtherContainer();
                            modified++;
                        }
                    } catch (Exception ignored) {}
                }

                // Add to player's global default-friend list if not already present
                PlayerSettingsHandler settings = new PlayerSettingsHandler(player);
                if (!settings.containsFriend(targetUuid)) {
                    settings.addFriend(targetUuid);
                }

                final int count = modified;
                player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                    Translator.get(TranslationKey.MESSAGES__FRIENDS_ADDALL_SUCCESS)
                        .replace("{player}", targetName)
                        .replace("{count}", String.valueOf(count))));
            });
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2)
            return "addall".startsWith(args[1].toLowerCase()) ? List.of("addall") : Collections.emptyList();
        if (args.length == 3 && args[1].equalsIgnoreCase("addall"))
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                .toList();
        return Collections.emptyList();
    }

    private void sendAction(@NotNull Player player, @NotNull String text) {
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }
}
