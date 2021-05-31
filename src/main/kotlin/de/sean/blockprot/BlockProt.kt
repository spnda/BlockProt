package de.sean.blockprot

import de.sean.blockprot.bukkit.commands.BlockProtCommand
import de.sean.blockprot.bukkit.events.*
import de.sean.blockprot.bukkit.util.LockUtil
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

open class BlockProt : JavaPlugin() {
    companion object {
        lateinit var instance: BlockProt
        protected lateinit var metrics: Metrics
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

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()

        LockUtil.loadBlocksFromConfig(config)

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
