package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MoveEvent : Listener {
    @EventHandler
    fun playerMove(event: PlayerMoveEvent) {
        val player = event.player
        AfkPlayerManager.setLastActivity(player.uniqueId, System.currentTimeMillis())
        if (AfkPlayerManager.isAfk(player.uniqueId)) {
            AfkPlayerManager.setAfk(player.uniqueId, false)
            AfkPlayerManager.unmarkPlayerAfk(player)
        }
    }
}
