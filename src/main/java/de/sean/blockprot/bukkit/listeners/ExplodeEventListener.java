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
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ListIterator;

public class ExplodeEventListener implements Listener {
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        // BlockExplodeEvent happens *after* the block has exploded
        checkBlocks(event.blockList().listIterator());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getEntity().getWorld())) return;
        checkBlocks(event.blockList().listIterator());
    }

    private void checkBlocks(ListIterator<Block> it) {
        while (it.hasNext()) {
            Block b = it.next();
            if (BlockProt.getDefaultConfig().isLockableTileEntity(b.getType()) ||
                BlockProt.getDefaultConfig().isLockableBlock(b.getType())) {
                // Someone owns this block, block its destroying.
                final BlockNBTHandler handler = new BlockNBTHandler(b);
                if (handler.isProtected())
                    it.remove();
            }
            // adding a break here affects the while loop causing it to only check one block
        }
    }
}
