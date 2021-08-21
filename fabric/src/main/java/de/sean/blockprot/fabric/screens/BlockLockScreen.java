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

package de.sean.blockprot.fabric.screens;

import de.sean.blockprot.fabric.nbt.BlockNBTHandler;
import net.minecraft.block.BlockState;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class BlockLockScreen extends BlockProtFabricScreen {
    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                      the player to server this gui to
     */
    public BlockLockScreen(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X1, player, true);
    }

    @Override
    public int getRows() {
        return 1;
    }

    @Override
    public String getDefaultScreenName() {
        return "Block Lock";
    }

    public void fill(ServerPlayerEntity player, BlockState block, BlockNBTHandler handler) {

    }
}
