package de.sean.splugin.spigot.commands

import de.sean.splugin.SPlugin
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class LockExecutor : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val player = Bukkit.getPlayer(sender.name) ?: return false
        when (args[0]) {
            "place" -> {
                val config = SPlugin.instance.config
                config["Players." + player.uniqueId + ".NotLockOnPlace"] = !config.getBoolean("players." + player.uniqueId + ".notLockOnPlace")
                SPlugin.instance.saveConfig()
            }
            else -> return false
        }
        return false
    }
}
