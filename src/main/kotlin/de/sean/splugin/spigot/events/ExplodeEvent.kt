package de.sean.splugin.spigot.events

import de.sean.splugin.util.SLockUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplodeEvent : Listener {
    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.MINECART_TNT || e.entityType == EntityType.PRIMED_TNT) {
            val it = e.blockList().iterator()
            while (it.hasNext()) {
                val b = it.next()
                if (b.type == Material.CHEST) {
                    // Someone owns this chest, block its destroying.
                    if (NBTTileEntity(b.state).getStringList(SLockUtil.LOCK_ATTRIBUTE) != null) it.remove()
                }
            }
        } else if (e.entityType == EntityType.CREEPER) {
            // We don't want mob griefing but villagers use mob griefing to work
            // So we'll just prevent creepers from destroying blocks like this.
            e.isCancelled = true
        }
    }
}
