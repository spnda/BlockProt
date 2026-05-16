package de.sean.blockprot.bukkit.config;

import de.sean.blockprot.bukkit.BlockProtLogger;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.TranslationKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Granular per-world lockable-block configuration loaded from worlds.yml.
 *
 * Resolution order for a world when worlds_config_enabled=true:
 *   1. If the world appears in worlds.yml with enabled: false, protection is disabled.
 *   2. If the world appears in worlds.yml with enabled: true, its worlds.yml lists are used.
 *   3. If the world is not listed, global config.yml lists are used.
 *
 * scanAndPopulate() scans loaded worlds at startup and adds missing entries
 * with enabled: true and lists inherited from global config.yml. Existing
 * entries are never overwritten.
 */
public final class WorldsConfig {

    private record WorldEntry(boolean enabled, Set<Material> tileEntities,
                              Set<Material> blocks, Set<Material> shulkerBoxes, Set<Material> doors) {}

    private final Map<String, WorldEntry> worlds = new HashMap<>();

    // -------------------------------------------------------------------------
    // Construction from the already-loaded worlds.yml configuration.
    // -------------------------------------------------------------------------

    public WorldsConfig(@NotNull YamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("worlds");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            ConfigurationSection ws = section.getConfigurationSection(name);
            if (ws == null) continue;

            boolean enabled = ws.getBoolean("enabled", false);
            worlds.put(name.toLowerCase(), new WorldEntry(
                enabled,
                loadMaterials(ws, "lockable_tile_entities"),
                loadMaterials(ws, "lockable_blocks"),
                loadMaterials(ws, "lockable_shulker_boxes"),
                loadMaterials(ws, "lockable_doors")
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Non-destructive world scanning and population.
    // -------------------------------------------------------------------------

    /**
     * Scans loaded server worlds and adds missing entries to worlds.yml.
     * Each new world is created with enabled: true and lists inherited from
     * the global config.yml. Existing entries are never overwritten.
     *
     * @param file         The worlds.yml file on disk.
     * @param globalConfig Global config.yml, used to inherit default block lists.
     * @param logger       Plugin logger used for console messages.
     * @return             The updated configuration.
     */
    @NotNull
    public static YamlConfiguration scanAndPopulate(
            @NotNull File file,
            @NotNull FileConfiguration globalConfig,
            @NotNull Logger logger) {

        YamlConfiguration disk;
        if (file.exists()) {
            disk = new YamlConfiguration();
            try {
                disk.load(file);
            } catch (InvalidConfigurationException | java.io.IOException e) {
                logger.warning(Translator.get(TranslationKey.CONSOLE__WORLDS_YML_SYNTAX_ERROR)
                    .replace("{error}", e.getMessage()));
                BlockProtLogger.log("worlds-repair", "Syntax error in worlds.yml: " + e.getMessage());
                // Overwrite with the bundled default
                try {
                    var stream = de.sean.blockprot.bukkit.BlockProt.getInstance().getResource("worlds.yml");
                    if (stream != null) {
                        disk = YamlConfiguration.loadConfiguration(new java.io.BufferedReader(new java.io.InputStreamReader(stream)));
                        disk.save(file);
                        logger.info(Translator.get(TranslationKey.CONSOLE__WORLDS_YML_RESTORED));
                    } else {
                        disk = new YamlConfiguration();
                    }
                } catch (Exception ex) {
                    logger.warning(Translator.get(TranslationKey.CONSOLE__WORLDS_YML_RESTORE_FAILED)
                        .replace("{error}", ex.getMessage()));
                    disk = new YamlConfiguration();
                }
            }
        } else {
            disk = new YamlConfiguration();
        }

        List<World> serverWorlds = Bukkit.getWorlds();
        int added = 0;

        // Log scan start only to the session log to keep console clean.
        BlockProtLogger.log("worlds-scan", Translator.get(TranslationKey.WORLDS__SCAN_START)
            .replace("{count}", String.valueOf(serverWorlds.size())));

        for (World world : serverWorlds) {
            String key = "worlds." + world.getName();

            if (disk.contains(key)) continue; // Existing entries are administrator-owned.

            // New entries stay active so the core protection system cannot be disabled by accident.
            disk.set(key + ".enabled", true);

            // Inherit the global config.yml lists as the starting point.
            copyList(disk, globalConfig, key, "lockable_tile_entities");
            copyList(disk, globalConfig, key, "lockable_shulker_boxes");
            copyList(disk, globalConfig, key, "lockable_blocks");
            copyList(disk, globalConfig, key, "lockable_doors");

            // Send details ONLY to session log
            BlockProtLogger.log("worlds-scan", "  + '" + world.getName() + "' added (enabled: true, lists inherited from config.yml)");

            added++;
        }

        if (added > 0) {
            try {
                disk.save(file);
            } catch (IOException e) {
                logger.warning(Translator.get(TranslationKey.CONSOLE__WORLDS_YML_SAVE_FAILED)
                    .replace("{error}", e.getMessage()));
            }
        }

        // Single summary line in console; details are in the session log
        String summary = Translator.get(TranslationKey.WORLDS__SCAN_COMPLETE)
            .replace("{count}", String.valueOf(serverWorlds.size()))
            .replace("{added}", String.valueOf(added));
        logger.info(summary);
        BlockProtLogger.log("worlds-scan", summary);

        return disk;
    }

    private static void copyList(@NotNull YamlConfiguration target, @NotNull FileConfiguration source,
                                  @NotNull String worldKey, @NotNull String listKey) {
        List<?> list = source.getList(listKey);
        if (list != null && !list.isEmpty()) {
            target.set(worldKey + "." + listKey, list);
        } else {
            target.set(worldKey + "." + listKey, Collections.emptyList());
        }
    }

    // -------------------------------------------------------------------------
    // Materials.
    // -------------------------------------------------------------------------

    private Set<Material> loadMaterials(@NotNull ConfigurationSection section, @NotNull String key) {
        Set<Material> result = new HashSet<>();
        for (String name : section.getStringList(key)) {
            Material m = Material.matchMaterial(name);
            if (m != null) result.add(m);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Public queries.
    // -------------------------------------------------------------------------

    /** Returns true when the world has an enabled worlds.yml entry. */
    public boolean hasWorldConfig(@NotNull World world) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && e.enabled();
    }

    /** Returns true when the world has a disabled worlds.yml entry. */
    public boolean isWorldDisabled(@NotNull World world) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && !e.enabled();
    }

    public boolean isLockable(@NotNull World world, @NotNull Material type) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        if (e == null || !e.enabled()) return false;
        return e.tileEntities().contains(type) || e.blocks().contains(type)
            || e.shulkerBoxes().contains(type) || e.doors().contains(type);
    }

    public boolean isLockableTileEntity(@NotNull World world, @NotNull Material type) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && e.enabled() && e.tileEntities().contains(type);
    }

    public boolean isLockableBlock(@NotNull World world, @NotNull Material type) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && e.enabled() && (e.blocks().contains(type) || e.doors().contains(type));
    }

    public boolean isLockableShulkerBox(@NotNull World world, @NotNull Material type) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && e.enabled() && e.shulkerBoxes().contains(type);
    }

    public boolean isLockableDoor(@NotNull World world, @NotNull Material type) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && e.enabled() && e.doors().contains(type);
    }
}
