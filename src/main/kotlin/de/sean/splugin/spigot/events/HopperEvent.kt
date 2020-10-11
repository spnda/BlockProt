package de.sean.splugin.spigot.events

import de.sean.splugin.util.SLockUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType

class HopperEvent : Listener {
    @EventHandler
    fun onItemMove(event: InventoryMoveItemEvent) {
        if (event.destination.type == InventoryType.HOPPER) {
            // This is a hopper trying to pull from something.
            when (event.source.type) {
                InventoryType.CHEST, InventoryType.FURNACE, InventoryType.BARREL, InventoryType.SHULKER_BOX -> {
                    // This hopper is trying to pull from some inventory which *may* be locked.
                    // Note: we do not have to check for double chests, as both sides of a chest are individually locked.
                    val sourceLocation = event.source.location ?: return
                    val source = NBTTileEntity(sourceLocation.block.state)
                    if (!source.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)) event.isCancelled = true
                }
                else -> return
            }
        }
    }
}
