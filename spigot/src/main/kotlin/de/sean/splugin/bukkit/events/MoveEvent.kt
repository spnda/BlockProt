package de.sean.splugin.bukkit.events

import de.sean.splugin.util.SMessages.unmarkPlayerAFK
import de.sean.splugin.util.SUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MoveEvent : Listener {
    @EventHandler
    fun playerMove(event: PlayerMoveEvent) {
        val player = event.player
        SUtil.playerLastActivity[player.uniqueId] = System.currentTimeMillis()
        if (SUtil.afkPlayers[player.uniqueId]!!) {
            SUtil.afkPlayers[player.uniqueId] = false
            unmarkPlayerAFK(player)
        }
    }
}
