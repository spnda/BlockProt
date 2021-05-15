package de.sean.blockprot

import de.sean.blockprot.bukkit.commands.BlockProtCommand
import de.sean.blockprot.bukkit.events.*
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import de.tr7zw.nbtapi.utils.MinecraftVersion
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class BlockProt : JavaPlugin() {
    companion object {
        lateinit var instance: BlockProt
        lateinit var metrics: Metrics
        const val pluginId: Int = 9999
        const val defaultLanguageFile = "translations_en.yml"
    }

    private fun loadTranslation(fileName: String) {
        var file = File(dataFolder, fileName)
        /* Only save the resource if it exists inside the JAR and it has not
         * been saved to the dataFolder yet. */
        val resource = this.getResource(fileName)
        if (resource == null && !file.exists()) {
            Bukkit.getLogger().warning("Could not find language file: $fileName. Defaulting to $defaultLanguageFile")
            this.saveResource(defaultLanguageFile, true)
            file = File(dataFolder, defaultLanguageFile)
        } else if (resource != null && !file.exists()) {
            this.saveResource(fileName, true)
            if (!file.exists()) throw RuntimeException("Could not load language file: $fileName")
        }
        val config = YamlConfiguration.loadConfiguration(file)
        Translator.init(config)
    }

    /**
     * Get a list of [Material]s for a list of [String]s. This checks all values
     * in the [Material] enum and returns the materials which name is included in
     * the list of strings. If some values in [strings] were not found in [values],
     * a warning will be printed afterwards.
     */
    private fun <T : Enum<*>> loadEnumValuesFromStrings(values: Array<T>, strings: MutableList<String>): Set<T> {
        val ret = mutableSetOf<T>()
        for (value in values) {
            if (strings.contains(value.name)) {
                ret.add(value)
                strings.remove(value.name)
            }
        }
        if (strings.isNotEmpty()) logger.warning("Could not map these values to enum: $strings")
        return ret
    }

    /**
     * Loads all the different lists from the config.yml file and adds
     * them to the various lists in LockUtil.
     */
    private fun loadBlocksFromConfig() {
        if (config.contains("lockable_tile_entities")) {
            var tileEntities = config.getList("lockable_tile_entities")!!
            tileEntities = tileEntities.filterIsInstance<String>()
            val materials = loadEnumValuesFromStrings(Material.values(), tileEntities.toMutableList())
            LockUtil.lockableTileEntities.addAll(materials)
        }

        if (config.contains("lockable_shulker_boxes")) {
            var shulkerBoxes = config.getList("lockable_shulker_boxes")!!
            shulkerBoxes = shulkerBoxes.filterIsInstance<String>()
            val materials = loadEnumValuesFromStrings(Material.values(), shulkerBoxes.toMutableList())
            LockUtil.shulkerBoxes.addAll(materials)
        }

        LockUtil.lockableTileEntities.addAll(LockUtil.shulkerBoxes)

        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
            if (config.contains("lockable_blocks")) {
                var lockableBlocks = config.getList("lockable_blocks")!!
                lockableBlocks = lockableBlocks.filterIsInstance<String>()
                val materials = loadEnumValuesFromStrings(Material.values(), lockableBlocks.toMutableList())
                LockUtil.lockableBlocks.addAll(materials)
            }

            if (config.contains("lockable_doors")) {
                var lockableDoors = config.getList("lockable_doors")!!
                lockableDoors = lockableDoors.filterIsInstance<String>()
                val materials = loadEnumValuesFromStrings(Material.values(), lockableDoors.toMutableList())
                LockUtil.lockableDoors.addAll(materials)
            }

            LockUtil.lockableBlocks.addAll(LockUtil.lockableDoors)
        }
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()

        loadBlocksFromConfig()

        /* Save all translation files into the plugin directory. */
        var languageFileName = config.get("language_file")
        if (languageFileName == null || languageFileName !is String) {
            languageFileName = defaultLanguageFile
        }
        loadTranslation(languageFileName)

        /* Check for updates */
        Bukkit.getScheduler().runTaskAsynchronously(this, UpdateChecker(emptyList(), description))

        /* bStats Metrics */
        metrics = Metrics(this, pluginId)

        /* Register Events */
        val pm = Bukkit.getServer().pluginManager
        registerEvent(pm, BlockEvent(this))
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, HopperEvent())
        registerEvent(pm, InteractEvent())
        registerEvent(pm, InventoryEvent())
        registerEvent(pm, JoinEvent())
        registerEvent(pm, RedstoneEvent())

        registerCommand("blockprot", BlockProtCommand())

        super.onEnable()
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }

    private fun registerCommand(name: String, executor: TabExecutor) {
        this.getCommand(name)?.setExecutor(executor)
    }
}
