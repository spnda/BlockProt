/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
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
            when {
                LockUtil.isLockableInventory(event.source.type) -> {
                    // This hopper is trying to pull from some inventory which *may* be locked.
                    // Note: we do not have to check for double chests, as both sides of a chest are individually locked.
                    val sourceLocation = getBlock(event.source.holder ?: return) ?: return
                    val handler = BlockNBTHandler(sourceLocation)
                    if (!handler.getRedstone()) event.isCancelled = true
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
