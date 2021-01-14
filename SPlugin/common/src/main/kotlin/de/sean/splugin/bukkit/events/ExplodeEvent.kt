package de.sean.splugin.bukkit.events

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplodeEvent : Listener {
    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.CREEPER) {
            // We don't want mob griefing but villagers use mob griefing to work
            // So we'll just prevent creepers from destroying blocks like this.
            e.isCancelled = true
        }
    }
}
