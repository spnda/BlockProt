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
package de.sean.blockprot.bukkit.tasks

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.function.Consumer

class DoubleChestLocker(
    private val newHandler: BlockNBTHandler,
    val block: Block,
    private val player: Player,
    private val callback: Consumer<Boolean>
) : Runnable {
    override fun run() {
        val doubleChest = getDoubleChest(block, player.world)
        if (doubleChest == null) {
            callback.accept(true)
            return
        }
        val oldChestHandler = BlockNBTHandler(doubleChest.block)
        if (oldChestHandler.isProtected() && oldChestHandler.getOwner() != player.uniqueId.toString()) {
            callback.accept(false)
        } else {
            newHandler.setOwner(oldChestHandler.getOwner())
            newHandler.setAccess(oldChestHandler.getAccess())
            newHandler.setRedstone(oldChestHandler.getRedstone())
            callback.accept(true)
        }
    }
}
