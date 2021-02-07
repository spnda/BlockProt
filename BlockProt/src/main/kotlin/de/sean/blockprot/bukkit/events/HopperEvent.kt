package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import org.bukkit.block.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryHolder

class HopperEvent : Listener {
    @EventHandler
    fun onItemMove(event: InventoryMoveItemEvent) {
        if (event.destination.type == InventoryType.HOPPER) {
            // This is a hopper trying to pull from something.
            when (event.source.type) {
                in LockUtil.lockableInventories -> {
                    // This hopper is trying to pull from some inventory which *may* be locked.
                    // Note: we do not have to check for double chests, as both sides of a chest are individually locked.
                    val sourceLocation = getBlock(event.source.holder ?: return) ?: return
                    val handler = BlockLockHandler(sourceLocation)
                    if (!handler.getRedstone()) event.isCancelled = true
                }
                else -> return
            }
        }
    }

    /**
     * Get the block from a InventoryHolder, instead of just getting the block by location as before.
     *
     * TODO: This needs improvement and should be dynamic, as in adapting to `ListUtil.lockableInventories`.
     */
    private fun getBlock(holder: InventoryHolder): Block? {
        when (holder) {
            is Chest -> return holder.block
            is Furnace -> return holder.block
            is Smoker -> return holder.block
            is BlastFurnace -> return holder.block
            is Hopper -> return holder.block
            is Barrel -> return holder.block
            is BrewingStand -> return holder.block
            is ShulkerBox -> return holder.block
            else -> return null
        }
    }
}
