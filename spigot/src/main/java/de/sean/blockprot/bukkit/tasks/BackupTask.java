package de.sean.blockprot.bukkit.tasks;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.BlockProtLogger;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates a ZIP backup of the plugin data folder before critical operations.
 *
 * <p>Backup is ONLY created when existing data is detected (blockprot.db,
 * or any nbt data file). This prevents noise on completely fresh installations.
 * When a backup is made, the console receives a clear notice and a reminder to
 * review the new config options.</p>
 *
 * <p>Backups are saved to: {@code plugins/BlockProt/backups/YYYY-MM-DD_HH-mm.zip}</p>
 *
 * @since SP26
 */
public final class BackupTask implements Runnable {

    private static final SimpleDateFormat DATE_FMT  = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    private static final int              MAX_BACKUPS = 10;

    /** Files whose presence signals that the server has pre-existing plugin data. */
    private static final String[] DATA_SENTINELS = {
        "data.yml",
        "blockprot.db",
        "blockprot_audit.sqlite",
        "stats.yml"
    };

    private final File    dataFolder;
    /**
     * When true, the caller explicitly requested a backup (e.g. /bp reload).
     * When false, the backup is opportunistic: only runs if prior data exists.
     */
    private final boolean forced;

    public BackupTask(@NotNull File dataFolder) {
        this(dataFolder, false);
    }

    public BackupTask(@NotNull File dataFolder, boolean forced) {
        this.dataFolder = dataFolder;
        this.forced     = forced;
    }

    @Override
    public void run() {
        boolean hasPriorData = forced || hasPriorData(dataFolder);

        if (!hasPriorData) {
            // Fresh install — no backup needed, no noise in console.
            return;
        }

        File backupDir = new File(dataFolder, "backups");
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            BlockProt.getInstance().getLogger().warning(
                Translator.get(TranslationKey.CONSOLE__BACKUP_DIR_FAILED));
            return;
        }

        pruneOldBackups(backupDir);

        String timestamp = DATE_FMT.format(new Date());
        File   zipFile   = new File(backupDir, timestamp + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            addDirectory(dataFolder, dataFolder.getName(), zos, backupDir);

            // ── Console notice ────────────────────────────────────────────────
            BlockProtLogger.log("backup", "Pre-existing data detected. Backup created at "
                + zipFile.getAbsolutePath());
            BlockProt.getInstance().getLogger().info(
                Translator.get(TranslationKey.CONSOLE__BACKUP_CREATED)
                    .replace("{file}", zipFile.getName()));
            if (!forced) {
                BlockProt.getInstance().getLogger().info(
                    Translator.get(TranslationKey.CONSOLE__BACKUP_REVIEW_CONFIG));
            }
        } catch (IOException e) {
            BlockProt.getInstance().getLogger().warning(
                Translator.get(TranslationKey.CONSOLE__BACKUP_FAILED)
                    .replace("{error}", e.getMessage()));
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Returns true if any known data-sentinel file exists in the data folder,
     * meaning this is an upgrade rather than a fresh installation.
     */
    public static boolean hasPriorData(@NotNull File dataFolder) {
        for (String name : DATA_SENTINELS) {
            if (new File(dataFolder, name).exists()) return true;
        }
        File[] children = dataFolder.listFiles();
        if (children == null) return false;
        for (File child : children) {
            if (!child.isDirectory()) continue;
            String name = child.getName();
            if (name.equalsIgnoreCase("backups") || name.equalsIgnoreCase("lang")) continue;
            if (containsLegacyData(child)) return true;
        }
        return false;
    }

    private static boolean containsLegacyData(@NotNull File dir) {
        File[] children = dir.listFiles();
        if (children == null) return false;
        for (File child : children) {
            if (child.isDirectory()) {
                if (child.getName().equalsIgnoreCase("DIM-1") || child.getName().equalsIgnoreCase("DIM1")) return true;
                if (containsLegacyData(child)) return true;
                continue;
            }
            String name = child.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".yml") || name.endsWith(".db") || name.endsWith(".sqlite") || name.endsWith(".dat")) {
                return true;
            }
        }
        return false;
    }

    private void addDirectory(@NotNull File dir, @NotNull String base,
                              @NotNull ZipOutputStream zos, @NotNull File backupDir)
            throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            // Never recurse into the backups folder itself, and skip session logs.
            if (file.equals(backupDir) || file.getName().endsWith(".log")) continue;

            String entry = base + "/" + file.getName();
            if (file.isDirectory()) {
                addDirectory(file, entry, zos, backupDir);
            } else {
                zos.putNextEntry(new ZipEntry(entry));
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.transferTo(zos);
                }
                zos.closeEntry();
            }
        }
    }

    private void pruneOldBackups(@NotNull File backupDir) {
        File[] zips = backupDir.listFiles((d, name) -> name.endsWith(".zip"));
        if (zips == null || zips.length < MAX_BACKUPS) return;

        Arrays.sort(zips, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i <= zips.length - MAX_BACKUPS; i++) {
            //noinspection ResultOfMethodCallIgnored
            zips[i].delete();
        }
    }
}
