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
package de.sean.blockprot.bukkit.nbt

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.inventory.DoubleChestInventory

/**
 * @since 0.2.3
 */
object LockUtil {
    /**
     * Get the BlockState of the double chest of given [block].
     * @return The BlockState of the double chest, null if given [block] was not a chest.
     * @since 0.3.0
     */
    @JvmStatic
    fun getDoubleChest(block: Block, world: World): BlockState? {
        var doubleChest: DoubleChest? = null
        val chestState = block.state
        if (chestState is Chest) {
            val inventory = chestState.inventory
            if (inventory is DoubleChestInventory) {
                doubleChest = inventory.holder
            }
        }
        if (doubleChest == null) return null
        val second = doubleChest.location

        when {
            block.x > second.x -> second.subtract(.5, 0.0, 0.0)
            block.z > second.z -> second.subtract(0.0, 0.0, .5)
            else -> second.add(.5, 0.0, .5)
        }

        return world.getBlockAt(second).state
    }
}
