package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
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
                in LockUtil.lockableInventories -> {
                    // This hopper is trying to pull from some inventory which *may* be locked.
                    // Note: we do not have to check for double chests, as both sides of a chest are individually locked.
                    val sourceLocation = event.source.location ?: return
                    val handler = BlockLockHandler(sourceLocation.block)
                    if (!handler.getRedstone()) event.isCancelled = true
                }
                else -> return
            }
        }
    }
}
