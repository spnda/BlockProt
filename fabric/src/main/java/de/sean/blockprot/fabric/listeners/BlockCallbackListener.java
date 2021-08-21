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

package de.sean.blockprot.fabric.listeners;

import de.sean.blockprot.fabric.nbt.BlockNBTHandler;
import de.sean.blockprot.fabric.screens.BlockLockScreen;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.logging.Logger;

public class BlockCallbackListener implements UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isSpectator()) return ActionResult.PASS;
        var entity = world.getBlockEntity(hitResult.getBlockPos());

        if (entity != null && entity.getCachedState().getBlock() instanceof ChestBlock) {
            var blockHandler = new BlockNBTHandler(entity);

            if (!blockHandler.canAccess(player.getUuidAsString())) {
                return ActionResult.FAIL;
            } else {
                Logger.getLogger(this.getClass().getSimpleName()).info("Can access chest!");

                if (player.isSneaking() && player instanceof ServerPlayerEntity serverPlayer) {
                    new BlockLockScreen(serverPlayer).open();
                    return ActionResult.FAIL;
                }
            }
        }
        return ActionResult.PASS;
    }
}
