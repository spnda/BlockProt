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

package de.sean.blockprot.bukkit.listeners;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class HopperEventListener implements Listener {
    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() == null) return;
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getSource().getHolder())) return;
        if (event.getDestination().getType() == InventoryType.HOPPER
            && BlockProt.getDefaultConfig().isLockable(Material.HOPPER)) { // Only run if HOPPER is actually in the config.
            // This is a hopper trying to pull from something.
            if (BlockProt.getDefaultConfig().isLockableInventory(event.getSource().getType())) {
                Block source = getBlock(event.getSource().getHolder());
                if (source != null) {
                    BlockNBTHandler nbtHandler = new BlockNBTHandler(source);
                    if (nbtHandler.isProtected() && nbtHandler.getRedstoneHandler().getHopperProtection()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Get the block from a InventoryHolder, instead of just getting the block by location as before.
     */
    @Nullable
    private Block getBlock(InventoryHolder holder) {
        if (holder instanceof Container) return ((Container) holder).getBlock();
        else if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            if (doubleChest.getWorld() == null) return null;
            return doubleChest.getWorld().getBlockAt(doubleChest.getLocation());
        } else return null;
    }
}
