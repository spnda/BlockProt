package de.sean.blockprot

import de.sean.blockprot.bukkit.commands.BlockProtCommand
import de.sean.blockprot.bukkit.events.*
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
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
        lateinit var translator: Translator
        const val pluginId: Int = 9999
    }

    private fun loadTranslation(fileName: String) {
        val file = File(dataFolder, fileName)
        if (!file.exists()) throw RuntimeException("Could not load any translations. Possibly corrupt?")
        val config = YamlConfiguration.loadConfiguration(file)
        translator = Translator(config)
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()

        /* Save all translation files into the plugin directory. */
        var languageFileName = config.get("language_file")
        if (languageFileName == null || languageFileName !is String) {
            languageFileName = "translations_en.yml"
        }
        this.saveResource(languageFileName, true)
        loadTranslation(languageFileName)

        /* Check for updates */
        Bukkit.getScheduler().runTaskAsynchronously(this, UpdateChecker(false, description))

        /* bStats Metrics */
        metrics = Metrics(this, pluginId)

        /* Register Events */
        val pm = Bukkit.getServer().pluginManager
        registerEvent(pm, BlockEvent(this))
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, HopperEvent())
        registerEvent(pm, InteractEvent())
        registerEvent(pm, InventoryEvent())
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
