package de.sean.splugin.paper.events

import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.discord.Discord
import de.sean.splugin.util.Messages
import de.sean.splugin.util.PluginConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Logger

class PaperJoinEvent : Listener {
    @EventHandler
    fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val pluginConfig = PluginConfig.instance

        /* Format Join Message */
        if (!player.hasPlayedBefore()) {
            // The player just joined for the first time, introduce the player to the server.
            Messages.sendTitleMessage(player, pluginConfig.getRandomMessage("messages.welcome").replace("[player]", player.displayName), "")
        } else {
            // The player is not playing for the first time, just welcome the player.
            val message: String =
                if (pluginConfig.getMessageCount("messages.welcomeBack") > 0) pluginConfig.getRandomMessage("messages.welcomeBack")
                else pluginConfig.getRandomMessage("messages.welcome")

            Messages.sendTitleMessage(player, message.replace("[player]", player.displayName), "")
        }
        val joinMessage = pluginConfig.getRandomMessageOrDefault("messages.join", "[player] has joined the server!")
        event.joinMessage = joinMessage.replace("[player]", player.displayName)

        /* AFK */
        AfkPlayerManager.setLastActivity(player.uniqueId, System.currentTimeMillis())
        AfkPlayerManager.setAfk(player.uniqueId, false)

        /* Discord */
        val discord = Discord.instance
        if (discord.joinMessage) {
            discord.sendMessage(joinMessage.replace("[player]", player.name))
        }

        /* Log the client brand name when a player joins */
        Logger.getLogger("PaperJoinEvent").info("${player.displayName} has joined with ${player.clientBrandName}")
    }
}
