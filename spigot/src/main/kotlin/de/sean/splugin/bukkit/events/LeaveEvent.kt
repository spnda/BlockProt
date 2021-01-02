package de.sean.splugin.bukkit.events

import de.sean.splugin.SPlugin
import de.sean.splugin.util.SMessages.getRandomMessage
import de.sean.splugin.util.SUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class LeaveEvent : Listener {
    @EventHandler
    fun playerLeave(event: PlayerQuitEvent) {
        /* Format leave message */
        event.quitMessage = getRandomMessage("messages.leave").replace("[player]", event.player.name)

        /* AFK */
        SUtil.playerLastActivity.remove(event.player.uniqueId)
        SUtil.afkPlayers.remove(event.player.uniqueId)

        /* Discord */
        val discord = SPlugin.discord
        if (discord.leaveMessage) {
            discord.sendMessage(getRandomMessage("messages.leave").replace("[player]", event.player.name))
        }
    }
}
