/*
 * Copyright (C) 2021 - 2024 spnda
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
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

/**
 * Event listener to block piston extends/retracts from destroying
 * or moving protected blocks.
 *
 * @since 0.4.4
 */
public class PistonEventListener implements Listener {
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) {
            return;
        }

        for (Block block : event.getBlocks()) {
            // Check if the block is lockable
            if (BlockProt.getDefaultConfig().isLockableShulkerBox(block.getType())) {
                // Shulker boxes drop right away. Those will therefore not have
                // to be configured to disable redstone, but they will never be
                // pushable if locked.
                if (new BlockNBTHandler(block).isProtected()) {
                    event.setCancelled(true);
                    return;
                }
            } else if (BlockProt.getDefaultConfig().isLockable(block.getType())) {
                BlockNBTHandler nbtHandler = new BlockNBTHandler(block);
                // We check if the block is protected, because we only care
                // about locked blocks that have redstone disabled.
                if (nbtHandler.isProtected() && nbtHandler.getRedstoneHandler().getPistonProtection()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // We will not allow a piston to retract blocks that are locked
        // and have redstone disabled. A non-sticky piston obviously
        // can't pull back blocks, so we will ignore those.
        if (!event.isSticky()) return;
        for (Block block : event.getBlocks()) {
            // Check if the block is lockable. Shulker boxes can't be pulled back
            // so we don't do the same as in the onPistonExtend event handler.
            if (BlockProt.getDefaultConfig().isLockable(block.getType())) {
                BlockNBTHandler nbtHandler = new BlockNBTHandler(block);
                // We check if the block is protected, because we only care
                // about locked blocks that have redstone disabled.
                if (nbtHandler.isProtected() && nbtHandler.getRedstoneHandler().getPistonProtection()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
