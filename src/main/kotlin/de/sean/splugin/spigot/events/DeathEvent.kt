package de.sean.splugin.spigot.events

import de.sean.splugin.SPlugin
import de.sean.splugin.util.SMessages.getRandomMessage
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class DeathEvent : Listener {
    @EventHandler
    fun playerDeath(event: PlayerDeathEvent) {
        // Some more formatting in the future?
        val message = getRandomMessage("messages.death").replace("[message]", event.deathMessage!!).replace("[player]", event.entity.displayName)
        event.deathMessage = ChatColor.RED.toString() + message

        /* Discord */SPlugin.discord.sendMessage(message)
    }
}
