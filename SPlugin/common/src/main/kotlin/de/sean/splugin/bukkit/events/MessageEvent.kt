package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.discord.Discord
import de.sean.splugin.util.PluginConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class MessageEvent : Listener {
    @EventHandler
    fun playerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val message = event.message

        /* Chat Message formatting */
        val config = PluginConfig.instance.configuration
        if (config.getBoolean("features.chatFormat")) {
            if (config.getString("chatFormat.format") != null) {
                val format = config.getString("chatFormat.format") ?: "[player]: [message]"
                event.format = format.replace("[player]", "%1\$s").replace("[message]", "%2\$s")
            }
        }

        /* AFK: We unmark the player AFK when they write a message. */
        AfkPlayerManager.setLastActivity(player.uniqueId, System.currentTimeMillis())
        if (AfkPlayerManager.isAfk(player.uniqueId)) {
            AfkPlayerManager.setAfk(player.uniqueId, false)
            AfkPlayerManager.unmarkPlayerAfk(player)
        }

        /* Discord */
        Discord.instance.sendMessage("**" + player.name + "**: " + message)
    }
}
