package de.sean.splugin.paper.events

import de.sean.splugin.bukkit.events.JoinEvent
import de.sean.splugin.paper.tasks.ClientBrandLogger
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class PaperJoinEvent(val plugin: JavaPlugin) : JoinEvent() {
    @EventHandler
    override fun playerJoin(event: PlayerJoinEvent) {
        super.playerJoin(event)

        /* Log the client brand name when a player joins */
        Bukkit.getScheduler().runTaskLater(plugin, ClientBrandLogger(event.player), 20) // 20 ticks = 1 second
    }
}
