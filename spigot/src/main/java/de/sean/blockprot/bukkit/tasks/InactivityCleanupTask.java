package de.sean.blockprot.bukkit.tasks;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.StatHandler;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Task that releases protected blocks from inactive players.
// Only activated if inactivity_cleanup_days > 0 in config.yml.
// Runs once at server startup on an async thread.
public final class InactivityCleanupTask implements Runnable {

    private final long thresholdMs;

    public InactivityCleanupTask(int days) {
        this.thresholdMs = TimeUnit.DAYS.toMillis(days);
    }

    @Override
    public void run() {
        long cutoff = System.currentTimeMillis() - thresholdMs;
        int total = 0;

        // Iterate all known players from the statistics cache
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.isOnline()) continue;
            if (offline.getLastSeen() > cutoff) continue; // Active player, skip

            try {
                var stat = new PlayerBlocksStatistic();
                if (offline.getPlayer() != null) {
                    StatHandler.getStatistic(stat, offline.getPlayer());
                } else {
                    // Offline player: attempt to load NBT from disk.
                    // If not available, skip silently.
                    continue;
                }

                for (var entry : stat.get()) {
                    var loc = entry.get();
                    if (loc.getWorld() == null) continue;
                    try {
                        var block = loc.getWorld().getBlockAt(loc);
                        if (!BlockProt.getDefaultConfig().isLockable(block.getType())) continue;
                        var handler = new BlockNBTHandler(block);
                        if (handler.isOwner(offline.getUniqueId())) {
                            handler.clear();
                            handler.applyToOtherContainer();
                            total++;
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        final int freed = total;
        Bukkit.getScheduler().runTask(BlockProt.getInstance(), () -> {
            if (freed > 0) {
                String msg = Translator.get(TranslationKey.MESSAGES__INACTIVITY_CLEANUP_DONE)
                    .replace("{count}", String.valueOf(freed));
                BlockProt.getInstance().getLogger().info(msg.replaceAll("(?i)§[0-9A-FK-ORX]", ""));
                Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("blockprot.admin"))
                    .forEach(p -> p.sendMessage(LegacyComponentSerializer.legacySection().deserialize(msg)));
            }
        });
    }
}
