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

package de.sean.blockprot.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;

public class BlockProt implements DedicatedServerModInitializer {
    public static final String MOD_ID = "blockprot";

    // public static ScreenHandlerType<BlockLockScreen> LOCK_SCREEN_HANDLER;

    @Override
    public void onInitializeServer() {
        // LOCK_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "lock"), BlockLockScreen::new);

        // UseBlockCallback.EVENT.register(new BlockCallbackListener());

        /*BlockPlaceEvent.EVENT.register((livingEntity, pos, block) -> {
            Logger.getLogger(BlockProt.class.getSimpleName()).info("Block placed: " + block.toString());
            if (block.getBlock() instanceof BlockWithEntity && livingEntity instanceof PlayerEntity player) {
                var settingsHandler = new PlayerSettingsHandler(player);
                if (settingsHandler.getLockOnPlace()) {
                    BlockEntity entity = livingEntity.world.getBlockEntity(pos);
                    if (entity == null) return ActionResult.PASS;

                    // Finally, copy over the default values.
                    var blockHandler = new BlockNBTHandler(entity);
                    blockHandler.lockBlock(player);
                    List<String> friends = settingsHandler.getDefaultFriends();
                    for (String friend : friends) {
                        blockHandler.addFriend(friend);
                    }
                    Logger.getLogger(BlockProt.class.getSimpleName()).info("Locked block!");
                }
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            var item = player.getStackInHand(hand);
            if (item.getItem() instanceof BlockItem) {
                // Only check for items being placed.
                var settingsHandler = new PlayerSettingsHandler(player);
                if (settingsHandler.getLockOnPlace()) {

                }
            }
            return TypedActionResult.pass(item);
        });*/
    }
}
