package de.sean.splugin

import de.sean.splugin.bukkit.events.*
import de.sean.splugin.bukkit.tasks.AfkChecker
import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.discord.DefaultDiscordEventHandler
import de.sean.splugin.discord.Discord
import de.sean.splugin.paper.events.*
import de.sean.splugin.util.PluginConfig
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class SPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: SPlugin
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()
        /* Config */
        val config: FileConfiguration = instance.config
        PluginConfig(config)

        AfkPlayerManager.init(config)

        if (config.getBoolean("features.afk")) Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(this, AfkChecker(), 0L, 20L)

        /* Events & Commands */
        registerEvents(Bukkit.getServer().pluginManager)
        registerCommands()

        /* Discord */
        Discord(config)
        Discord.instance.addEventListener(DefaultDiscordEventHandler())

        super.onEnable()
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this)
        super.onDisable()
    }

    private fun registerEvents(pm: PluginManager) {
        // register all events
        registerEvent(pm, BlockEvent(this))
        registerEvent(pm, DeathEvent())
        registerEvent(pm, DismountEvent())
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, HopperEvent())
        registerEvent(pm, PaperInteractEvent())
        registerEvent(pm, InventoryEvent())
        registerEvent(pm, PaperJoinEvent(this))
        registerEvent(pm, LeaveEvent())
        registerEvent(pm, MessageEvent())
        registerEvent(pm, MoveEvent())
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }

    private fun registerCommands() {
        // register any commands we want to use
    }
}
