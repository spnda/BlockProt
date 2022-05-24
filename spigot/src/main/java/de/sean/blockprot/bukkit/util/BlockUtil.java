/*
 * Copyright (C) 2021 - 2022 spnda
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

package de.sean.blockprot.bukkit.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Various utilities for manipulating and getting block
 * related data.
 *
 * @since 0.4.6
 */
public final class BlockUtil {
    /**
     * Get the BlockState of the double chest of given {@code block}.
     *
     * @param block The other half of the chest. Can be any block, this will check for
     *              it.
     * @return The BlockState of the double chest, null if given {@code block} was not a chest.
     * @since 0.4.6
     */
    @Nullable
    public static BlockState getDoubleChest(@NotNull final Block block) {
        // Get the double chest inventory holder.
        DoubleChest doubleChest = null;
        final BlockState chestState = block.getState();
        if (chestState instanceof Chest) {
            final Inventory chestInventory = ((Chest) chestState).getInventory();
            if (chestInventory instanceof DoubleChestInventory) {
                doubleChest = ((DoubleChestInventory) chestInventory).getHolder();
            }
        }
        if (doubleChest == null) return null;

        // Get the location of the other side of the chest.
        // "middle" is referring to exactly the middle between
        // the two sides of the chest.
        final Location middle = doubleChest.getLocation();
        if (block.getX() > middle.getX()) {
            middle.subtract(.5, .0, .0);
        } else if (block.getZ() > middle.getZ()) {
            middle.subtract(.0, .0, .5);
        } else {
            middle.add(.5, .0, .5);
        }

        return block.getWorld().getBlockAt(middle).getState();
    }

    /**
     * Gets the other half of the door, as per the block
     * given.
     *
     * @param state The BlockState of the door half.
     * @return Returns the other half of the door, or null
     * if the given state is not a door.
     * @since 0.4.6
     */
    @Nullable
    public static Block getOtherDoorHalf(@NotNull final BlockState state) {
        try {
            final Door door = (Door) state.getBlockData();
            final Location other = state.getLocation().clone();
            if (door.getHalf() == Bisected.Half.TOP) other.setY(other.getY() - 1);
            else other.setY(other.getY() + 1);
            return state.getWorld().getBlockAt(other);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
