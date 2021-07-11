/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.sean.blockprot.bukkit.listeners

import de.sean.blockprot.bukkit.BlockProt
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import org.bukkit.block.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.InventoryHolder

class HopperEventListener : Listener {
    @EventHandler
    fun onItemMove(event: InventoryMoveItemEvent) {
        if (BlockProt.getDefaultConfig().isWorldExcluded((event.source.holder as BlockInventoryHolder).block.world)) return
        if (event.destination.type == InventoryType.HOPPER) {
            // This is a hopper trying to pull from something.
            when {
                BlockProt.getDefaultConfig().isLockableInventory(event.source.type) -> {
                    // This hopper is trying to pull from some inventory which *may* be locked.
                    // Note: we do not have to check for double chests, as both sides of a chest are individually locked.
                    val sourceLocation = getBlock(event.source.holder ?: return) ?: return
                    val handler = BlockNBTHandler(sourceLocation)
                    if (!handler.redstone) event.isCancelled = true
                }
                else -> return
            }
        }
    }

    /**
     * Get the block from a InventoryHolder, instead of just getting the block by location as before.
     */
    private fun getBlock(holder: InventoryHolder): Block? {
        return when (holder) {
            is Chest -> holder.block
            is Furnace -> holder.block
            is Smoker -> holder.block
            is BlastFurnace -> holder.block
            is Hopper -> holder.block
            is Barrel -> holder.block
            is BrewingStand -> holder.block
            is ShulkerBox -> holder.block
            is DoubleChest -> holder.world?.getBlockAt(holder.location)
            else -> null
        }
    }
}
