package de.sean.splugin

import de.sean.splugin.discord.DiscordUtil
import de.sean.splugin.spigot.commands.LockExecutor
import de.sean.splugin.spigot.events.*
import de.sean.splugin.spigot.tasks.AfkChecker
import de.sean.splugin.spigot.tasks.SleepChecker
import de.sean.splugin.util.PlayerType
import de.sean.splugin.util.SUtil
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.LinkOption

class SPlugin : org.bukkit.plugin.java.JavaPlugin() {
    override fun onEnable() {
        /* Config */
        this.also { instance = it }.saveDefaultConfig()
        val config: FileConfiguration = instance.config
        PlayerType.loadFromConfig(config)
        if (config.getBoolean("features.skipNight")) Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(this, SleepChecker(), 0L, 40L)
        if (config.getBoolean("features.afk")) Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(this, AfkChecker(), 0L, 20L)
        registerEvents(Bukkit.getServer().pluginManager)
        registerCommands()

        for (player in Bukkit.getOnlinePlayers()) {
            SUtil.afkPlayers[player.uniqueId] = false;
            SUtil.playerLastActivity[player.uniqueId] = System.currentTimeMillis();
        }

        /* Discord */
        discord = DiscordUtil(config)
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this)
    }

    private fun registerEvents(pm: org.bukkit.plugin.PluginManager) {
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
        getCommand("lock")?.setExecutor(LockExecutor())
    }

    companion object {
        lateinit var discord: DiscordUtil
        lateinit var instance: SPlugin
    }
}
