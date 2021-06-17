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
package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import java.util.logging.Logger

class ExplodeEvent : Listener {
    @EventHandler
    fun onBlockExplode(e: BlockExplodeEvent) {
        Logger.getLogger(this.javaClass.simpleName).info(e.block.type.toString())
        // BlockExplodeEvent happens *after* the block has exploded
        checkBlocks(e.blockList().iterator())
    }

    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        checkBlocks(e.blockList().iterator())
    }

    private fun checkBlocks(it: MutableIterator<Block>) {
        while (it.hasNext()) {
            val b = it.next()
            when {
                LockUtil.isLockableTileEntity(b.type) || LockUtil.isLockableBlock(b.type) -> {
                    // Someone owns this block, block its destroying.
                    val handler = BlockNBTHandler(b)
                    if (handler.isProtected()) it.remove()
                }
                // adding a break here affects the while loop causing it to only check one block
                else -> {
                }
            }
        }
    }
}
