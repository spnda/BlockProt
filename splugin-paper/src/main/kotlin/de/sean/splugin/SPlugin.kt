package de.sean.splugin

import de.sean.splugin.bukkit.events.*
import de.sean.splugin.bukkit.tasks.AfkChecker
import de.sean.splugin.discord.DefaultDiscordEventHandler
import de.sean.splugin.discord.Discord
import de.sean.splugin.util.PluginConfig
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import sun.plugin2.message.Message

class SPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: SPlugin
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()
        /* Config */
        val config: FileConfiguration = instance.config
        PluginConfig(config)

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
        pm.registerEvents(BlockEvent(), this)
        pm.registerEvents(DeathEvent(), this)
        pm.registerEvents(DismountEvent(), this)
        pm.registerEvents(ExplodeEvent(), this)
        pm.registerEvents(InteractEvent(), this)
        pm.registerEvents(JoinEvent(), this)
        pm.registerEvents(LeaveEvent(), this)
        pm.registerEvents(MessageEvent(), this)
        pm.registerEvents(MoveEvent(), this)
    }

    private fun registerCommands() {
        // register any commands we want to use
    }
}
