package de.sean.splugin.bukkit.events

import de.sean.splugin.discord.Discord
import de.sean.splugin.util.PluginConfig
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathEvent : Listener {
    @EventHandler
    fun playerDeath(event: PlayerDeathEvent) {
        // Some more formatting in the future?
        val pluginConfig = PluginConfig.instance
        val message = pluginConfig.getRandomMessage("messages.death")
            .replace("[message]", event.deathMessage!!).replace("[player]", event.entity.displayName)
        if (message.isNotEmpty()) event.deathMessage = ChatColor.RED.toString() + message

        /* Discord */
        Discord.instance.sendMessage(message)
    }
}
