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
package de.sean.blockprot.bukkit.tasks

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.util.BlockUtil.getDoubleChest
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
        val doubleChest = getDoubleChest(block)
        if (doubleChest == null) {
            callback.accept(true)
            return
        }
        val oldChestHandler = BlockNBTHandler(doubleChest.block)
        if (oldChestHandler.isProtected && oldChestHandler.owner != player.uniqueId.toString()) {
            callback.accept(false)
        } else {
            newHandler.mergeHandler(oldChestHandler)
            callback.accept(true)
        }
    }
}
