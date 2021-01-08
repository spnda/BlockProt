package de.sean.splugin

import de.sean.splugin.bukkit.events.*
import de.sean.splugin.bukkit.tasks.AfkChecker
import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.discord.DefaultDiscordEventHandler
import de.sean.splugin.discord.Discord
import de.sean.splugin.util.PluginConfig
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.PluginManager

class SPlugin : org.bukkit.plugin.java.JavaPlugin() {
    override fun onEnable() {
        /* Config */
        this.also { instance = it }.saveDefaultConfig()
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
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this)
    }

    private fun registerEvents(pm: PluginManager) {
        pm.registerEvents(BlockEvent(), this) // Handles the placement/destruction of blocks by players
        pm.registerEvents(DeathEvent(), this) // Handles every player death event
        pm.registerEvents(DismountEvent(), this) // Handles every entity dismount
        pm.registerEvents(ExplodeEvent(), this) // Handles every explosion in the world
        pm.registerEvents(InteractEvent(), this) // Handles every block interaction by a player
        pm.registerEvents(InventoryEvent(), this) // Handles every inventory interaction
        pm.registerEvents(JoinEvent(), this) // Handles every user join event
        pm.registerEvents(LeaveEvent(), this) // Handles every user leave event
        pm.registerEvents(MessageEvent(), this) // Handles every chat message event
        pm.registerEvents(MoveEvent(), this) // Handles every move of a player
    }

    private fun registerCommands() {
        // getCommand("lock")?.setExecutor(LockExecutor())
    }

    companion object {
        lateinit var instance: SPlugin
    }
}
