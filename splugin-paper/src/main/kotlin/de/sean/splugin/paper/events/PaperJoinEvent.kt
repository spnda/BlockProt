package de.sean.splugin.paper.events

import de.sean.splugin.bukkit.events.JoinEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Logger

class PaperJoinEvent : JoinEvent() {
    @EventHandler
    override fun playerJoin(event: PlayerJoinEvent) {
        super.playerJoin(event)

        /* Log the client brand name when a player joins */
        Logger.getLogger(this.javaClass.simpleName).info("${event.player.displayName} has joined with ${event.player.clientBrandName}")
    }
}
