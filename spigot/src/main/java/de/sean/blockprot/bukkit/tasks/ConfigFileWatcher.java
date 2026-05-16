package de.sean.blockprot.bukkit.tasks;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Watches the plugin data directory and reloads BlockProt after relevant YAML
 * files change. Reloads are debounced to avoid duplicate reloads while an
 * editor is still writing the file.
 */
public final class ConfigFileWatcher implements Runnable {

    private static final long DEBOUNCE_MS = 2000;

    private final BlockProt plugin;
    private final File watchDir;
    private final AtomicLong lastEventTime = new AtomicLong(0);
    private boolean reloadScheduled = false;

    private WatchService watchService;

    public ConfigFileWatcher(@NotNull BlockProt plugin) {
        this.plugin = plugin;
        this.watchDir = plugin.getDataFolder();
    }

    public void start() {
        Thread thread = new Thread(this, "BlockProt-FileWatcher");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        try {
            if (watchService != null) watchService.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dir = watchDir.toPath();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            File langDir = new File(watchDir, "lang");
            if (langDir.exists() && langDir.isDirectory()) {
                langDir.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            }

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    String name = changed.getFileName().toString();

                    if (name.equals("config.yml") || name.equals("worlds.yml") || name.endsWith(".yml")) {
                        lastEventTime.set(System.currentTimeMillis());
                        scheduleReload();
                    }
                }

                if (!key.reset()) break;
            }
        } catch (InterruptedException | ClosedWatchServiceException ignored) {
            // Plugin shutdown.
        } catch (Exception e) {
            plugin.getLogger().warning(Translator.get(TranslationKey.CONSOLE__FILEWATCHER_ERROR)
                .replace("{error}", e.getMessage()));
        }
    }

    private synchronized void scheduleReload() {
        if (reloadScheduled) return;
        reloadScheduled = true;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            long timeSinceLastEvent = System.currentTimeMillis() - lastEventTime.get();
            if (timeSinceLastEvent >= DEBOUNCE_MS - 100) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info(Translator.get(TranslationKey.CONSOLE__CONFIG_CHANGE_DETECTED));
                    plugin.reloadConfigAndTranslations();
                    plugin.getLogger().info(Translator.get(TranslationKey.CONSOLE__CONFIG_RELOADED));
                });
            }
            synchronized (this) {
                reloadScheduled = false;
            }
        }, (DEBOUNCE_MS / 50) + 5L);
    }
}
