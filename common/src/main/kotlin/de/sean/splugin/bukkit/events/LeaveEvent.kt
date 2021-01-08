package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.discord.Discord
import de.sean.splugin.util.PluginConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class LeaveEvent : Listener {
    @EventHandler
    fun playerLeave(event: PlayerQuitEvent) {
        val pluginConfig = PluginConfig.instance

        /* Format leave message */
        val quitMessage = pluginConfig.getRandomMessageOrDefault("messages.join", "[player] has left the server!") // Default quit message
        event.quitMessage = quitMessage.replace("[player]", event.player.name)

        /* AFK */
        AfkPlayerManager.remove(event.player)

        /* Discord */
        val discord = Discord.instance
        if (discord.leaveMessage) {
            discord.sendMessage(quitMessage.replace("[player]", event.player.name))
        }
    }
}
