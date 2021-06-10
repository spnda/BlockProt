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

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
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
                    val handler = BlockLockHandler(b)
                    if (handler.isProtected()) it.remove()
                }
                // adding a break here affects the while loop causing it to only check one block
                else -> {
                }
            }
        }
    }
}
