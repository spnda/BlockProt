package de.sean.blockprot.bukkit.config;

import de.sean.blockprot.bukkit.BlockProtLogger;
import de.sean.blockprot.bukkit.TranslationKey;
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
 * Configuracion granular por mundo leida de worlds.yml.
 *
 * Jerarquia de resolucion para un mundo dado cuando worlds_config_enabled=true:
 *   1. Si el mundo aparece en worlds.yml con enabled: false, no hay proteccion.
 *   2. Si el mundo aparece en worlds.yml con enabled: true, usa las listas de worlds.yml exclusivamente.
 *   3. Si el mundo no aparece en worlds.yml, usa las listas globales de config.yml.
 *
 * El metodo scanAndPopulate() escanea los mundos cargados al arrancar y anade entradas faltantes
 * con enabled: true y las listas heredadas del config.yml global. Nunca sobrescribe entradas existentes.
 */
public final class WorldsConfig {

    private record WorldEntry(boolean enabled, Set<Material> tileEntities,
                              Set<Material> blocks, Set<Material> shulkerBoxes, Set<Material> doors) {}

    private final Map<String, WorldEntry> worlds = new HashMap<>();

    // -------------------------------------------------------------------------
    // Construcción desde YamlConfiguration (ya cargada del disco)
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
    // Escaneo y auto-población (no-destructivo)
    // -------------------------------------------------------------------------

    /**
     * Escanea los mundos cargados en el servidor y anade en worlds.yml los que falten.
     * Cada mundo nuevo se crea con enabled: true y las listas heredadas del config.yml global.
     * No sobreescribe entradas existentes.
     *
     * @param file         El archivo worlds.yml en disco.
     * @param globalConfig El config.yml global, para heredar las listas de bloques por defecto.
     * @param logger       Logger del plugin para mensajes de consola.
     * @return             La configuración actualizada (para que BlockProt pueda recargar el objeto).
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

        // Log scan start only to session log — keeps console clean
        BlockProtLogger.log("worlds-scan", Translator.get(TranslationKey.WORLDS__SCAN_START)
            .replace("{count}", String.valueOf(serverWorlds.size())));

        for (World world : serverWorlds) {
            String key = "worlds." + world.getName();

            if (disk.contains(key)) continue; // Ya existe — respetar configuración del admin

            // Crear la entrada como activa para no romper la proteccion base.
            disk.set(key + ".enabled", true);

            // Heredar listas del config.yml global como punto de partida
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
    // Materiales
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
    // Consultas públicas
    // -------------------------------------------------------------------------

    /** True si el mundo tiene entrada en worlds.yml con enabled: true. */
    public boolean hasWorldConfig(@NotNull World world) {
        WorldEntry e = worlds.get(world.getName().toLowerCase());
        return e != null && e.enabled();
    }

    /** True si el mundo tiene entrada en worlds.yml con enabled: false (sin protección). */
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
