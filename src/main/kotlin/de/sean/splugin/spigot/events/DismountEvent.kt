package de.sean.splugin.spigot.events

import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.util.Vector
import org.spigotmc.event.entity.EntityDismountEvent

class DismountEvent : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun entityDismount(event: EntityDismountEvent) {
        // This indicates a player just stopped sitting.
        if (event.entity is Player && event.dismounted is Arrow) {
            event.dismounted.remove()
            event.entity.setVelocity(Vector(.0f, .5f, .0f))
        }
    }
}
