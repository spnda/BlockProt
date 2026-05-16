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

// Tarea que libera bloques protegidos de jugadores inactivos.
// Solo se activa si inactivity_cleanup_days > 0 en config.yml.
// Se ejecuta una vez al arrancar el servidor, en hilo async.
public final class InactivityCleanupTask implements Runnable {

    private final long thresholdMs;

    public InactivityCleanupTask(int days) {
        this.thresholdMs = TimeUnit.DAYS.toMillis(days);
    }

    @Override
    public void run() {
        long cutoff = System.currentTimeMillis() - thresholdMs;
        int total = 0;

        // Iterar todos los jugadores conocidos en el caché de estadísticas
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.isOnline()) continue;
            if (offline.getLastSeen() > cutoff) continue; // Jugador activo, saltar

            try {
                var stat = new PlayerBlocksStatistic();
                if (offline.getPlayer() != null) {
                    StatHandler.getStatistic(stat, offline.getPlayer());
                } else {
                    // Jugador offline — intentamos cargar NBT desde disco
                    // Si no está disponible, saltamos silenciosamente
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
                BlockProt.getInstance().getLogger().info(msg.replaceAll("§.", ""));
                Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("blockprot.admin"))
                    .forEach(p -> p.sendMessage(LegacyComponentSerializer.legacySection().deserialize(msg)));
            }
        });
    }
}
