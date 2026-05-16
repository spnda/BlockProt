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
import java.util.Locale;

/**
 * Handles the {@code /blockprot friends} subcommand.
 *
 * <pre>
 * /bp friends                  - opens the friend-list GUI.
 * /bp friends addall <player>  - adds <player> to every protected block owned by the executor.
 * </pre>
 */
public final class FriendsAddAllCommand implements CommandExecutor {

    private static final long SEARCH_MSG_TIMEOUT_TICKS = 60L;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(
                Translator.get(TranslationKey.MESSAGES__ONLY_PLAYERS)));
            return true;
        }
        if (args.length == 1) {
            openFriendManageGui(player);
            return true;
        }

        if (args.length < 3 || !isAddAllAlias(args[1])) {
            sendAction(player, Translator.get(TranslationKey.MESSAGES__FRIENDS_ADDALL_USAGE));
            return true;
        }

        handleAddAllCommand(player, args[2]);
        return true;
    }

    private void openFriendManageGui(@NotNull Player player) {
        InventoryState state = new InventoryState(null);
        state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH;
        InventoryState.set(player.getUniqueId(), state);
        var inv = new FriendManageInventory().fill(player);
        if (inv != null) player.openInventory(inv);
    }

    private void handleAddAllCommand(@NotNull Player player, @NotNull String targetName) {
        sendAction(player, Translator.get(TranslationKey.MESSAGES__FRIENDS_SEARCHING)
            .replace("{player}", targetName));

        final BukkitTask[] clearTask = {null};
        clearTask[0] = Bukkit.getScheduler().runTaskLater(BlockProt.getInstance(),
            () -> player.sendActionBar(Component.empty()), SEARCH_MSG_TIMEOUT_TICKS);

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), () -> {
            OfflinePlayer target = resolveOfflinePlayer(targetName);

            if (clearTask[0] != null) {
                clearTask[0].cancel();
                clearTask[0] = null;
            }

            if (target == null || target.getUniqueId() == null) {
                Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                    sendAction(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                    Bukkit.getScheduler().runTaskLater(BlockProt.getInstance(),
                        () -> player.sendActionBar(Component.empty()), SEARCH_MSG_TIMEOUT_TICKS);
                });
                return;
            }

            final String targetUuid = target.getUniqueId().toString();

            Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
                player.sendActionBar(Component.empty());
                int modified = applyAddAllToOwnedBlocks(player, targetUuid);

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
    }

    @Nullable
    private OfflinePlayer resolveOfflinePlayer(@NotNull String name) {
        OfflinePlayer target = PlayerNameResolver.findOfflinePlayer(name);
        if (target == null) {
            @SuppressWarnings("deprecation")
            OfflinePlayer fallback = Bukkit.getOfflinePlayer(name);
            if (fallback.hasPlayedBefore()) target = fallback;
        }
        return target;
    }

    private int applyAddAllToOwnedBlocks(@NotNull Player player, @NotNull String targetUuid) {
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
        return modified;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2)
            return isAddAllAliasPrefix(args[1]) ? List.of(preferredAddAllAlias()) : Collections.emptyList();
        if (args.length == 3 && isAddAllAlias(args[1]))
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                .toList();
        return Collections.emptyList();
    }

    private void sendAction(@NotNull Player player, @NotNull String text) {
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private boolean isAddAllAlias(@NotNull String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.equals("addall")) return true;
        try {
            return BlockProt.getDefaultConfig().isLocalizedCommandAliasesEnabled()
                && Translator.getLocale().getLanguage().equalsIgnoreCase("es")
                && normalized.equals("agregartodos");
        } catch (AssertionError ignored) {
            return false;
        }
    }

    private boolean isAddAllAliasPrefix(@NotNull String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return preferredAddAllAlias().startsWith(normalized) || "addall".startsWith(normalized);
    }

    @NotNull
    private String preferredAddAllAlias() {
        try {
            if (BlockProt.getDefaultConfig().isLocalizedCommandAliasesEnabled()
                && Translator.getLocale().getLanguage().equalsIgnoreCase("es")) {
                return "agregartodos";
            }
        } catch (AssertionError ignored) {}
        return "addall";
    }
}
