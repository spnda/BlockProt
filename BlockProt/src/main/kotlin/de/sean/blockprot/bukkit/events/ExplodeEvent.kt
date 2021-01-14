package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplodeEvent : Listener {
    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.MINECART_TNT || e.entityType == EntityType.PRIMED_TNT || e.entityType == EntityType.ENDER_CRYSTAL || e.entityType == EntityType.FIREBALL || e.entityType == EntityType.SMALL_FIREBALL) {
            val it = e.blockList().iterator()
            while (it.hasNext()) {
                val b = it.next()
                when (b.type) {
                    in LockUtil.lockableBlocks -> {
                        // Someone owns this block, block its destroying.
                        val handler = BlockLockHandler(NBTTileEntity(b.state))
                        if (handler.isProtected()) it.remove()
                    }
                    // adding a break here affects the while loop causing it to only check one block
                    else -> {}
                }
            }
        } else if (e.entityType == EntityType.CREEPER) {
            // We don't want mob griefing but villagers use mob griefing to work
            // So we'll just prevent creepers from destroying blocks like this.
            e.isCancelled = true
        }
    }
}
