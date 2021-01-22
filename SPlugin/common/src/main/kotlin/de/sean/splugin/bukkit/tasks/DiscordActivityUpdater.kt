package de.sean.splugin.bukkit.tasks

import de.sean.splugin.discord.Discord
import de.sean.splugin.util.Messages
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration

/**
 * Updates the discord activity with the amount of players currently on the server.
 * Requires [Discord] to be initialized before this can be run.
 */
class DiscordActivityUpdater(private val config: FileConfiguration) : Runnable {
    override fun run() {
        val players = Bukkit.getOnlinePlayers()
        if (Discord.isInitialized()) {
            var message = Messages.getRandomMessage(config.getList("discord.activities"))
            if (message.isEmpty()) return
            message = message.replace("[players]", players.size.toString(), true)
            Discord.instance.updateActivity(Activity.playing(message))
        }
    }
}
