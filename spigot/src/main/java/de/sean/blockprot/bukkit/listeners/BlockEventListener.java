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

import com.google.common.collect.Iterables;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BlockEventListener implements Listener {
    private final BlockProt blockProt;

    public BlockEventListener(@NotNull BlockProt blockProt) {
        this.blockProt = blockProt;
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getBlock().getType())) return;
        // The event hasn't been cancelled already, we'll check if need
        // to cancel it manually.
        BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());
        // If the block is protected by any user, prevent it from burning down.
        if (handler.isProtected()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getBlock().getType())) return; // We only want to check for lockable blocks.

        BlockNBTHandler handler = new BlockNBTHandler(event.getBlock());
        if (!handler.isOwner(event.getPlayer().getUniqueId().toString()) && handler.isProtected()) {
            // Prevent unauthorized players from breaking locked blocks.
            event.setCancelled(true);
        } else if (BlockProt.getDefaultConfig().isLockableShulkerBox(event.getBlock().getType()) && event.isDropItems()) {
            // The player can break the block. We will now check if its a shulker box,
            // so we can add NBT to the shulker box that it gets locked upon placing again.
            event.setDropItems(false); // Prevent the event from dropping items itself
            Collection<ItemStack> itemsToDrop = event.getBlock().getDrops();
            if (itemsToDrop.isEmpty()) return;
            ItemStack item = Iterables.getFirst(itemsToDrop, null); // Shulker blocks should only have a single drop anyway
            if (item == null) return;

            NBTCompound nbtTile = new NBTTileEntity(event.getBlock().getState()).getPersistentDataContainer();
            NBTItem nbtItem = new NBTItem(item, true);
            nbtItem.getOrCreateCompound("BlockEntityTag").getOrCreateCompound("PublicBukkitValues").mergeCompound(nbtTile);

            event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getBlock().getWorld())) return;
        if (!event.getPlayer().hasPermission(BlockNBTHandler.PERMISSION_LOCK)) return;
        Block block = event.getBlockPlaced();
        String playerUuid = event.getPlayer().getUniqueId().toString();

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            BlockNBTHandler handler = new BlockNBTHandler(block);

            // After placing, it takes 1 tick for the chests to connect.
            Bukkit.getScheduler().runTaskLater(
                this.blockProt,
                () -> {
                    handler.applyToOtherContainer(
                        (otherHandler) -> otherHandler.isNotProtected() || otherHandler.isOwner(playerUuid),
                        // We can't cancel the event 1 tick later, its already executed. We'll just need to destroy the block and drop it.
                        (otherHandler) -> event.getPlayer().getWorld().getBlockAt(block.getLocation()).breakNaturally()
                    );
                },
                1
            );

            PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(event.getPlayer());
            if (settingsHandler.getLockOnPlace()) {
                BlockLockOnPlaceEvent lockOnPlaceEvent = new BlockLockOnPlaceEvent(event.getBlock(), event.getPlayer());
                Bukkit.getPluginManager().callEvent(lockOnPlaceEvent);
                if (!lockOnPlaceEvent.isCancelled()) {
                    handler.lockBlock(event.getPlayer());
                    List<String> friends = settingsHandler.getDefaultFriends();
                    for (String friend : friends) {
                        handler.addFriend(friend);
                    }
                }

                if (BlockProt.getDefaultConfig().disallowRedstoneOnPlace()) {
                    handler.setRedstone(false);
                }
            }
        } else if (BlockProt.getDefaultConfig().isLockable(event.getBlock().getType())) {
            Bukkit.getLogger().info(event.getBlock().getType().toString());
            BlockNBTHandler handler = new BlockNBTHandler(block);
            // We only try to lock the block if it isn't locked already.
            // Shulker boxes might already be locked, from previous placing.
            if (handler.isNotProtected()) {
                BlockLockOnPlaceEvent lockOnPlaceEvent = new BlockLockOnPlaceEvent(event.getBlock(), event.getPlayer());
                Bukkit.getPluginManager().callEvent(lockOnPlaceEvent);
                if (!lockOnPlaceEvent.isCancelled()) {
                    // Assign an empty string for no owner to not have NPEs when reading
                    PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(event.getPlayer());
                    handler.setOwner(
                        settingsHandler.getLockOnPlace() ? playerUuid : ""
                    );
                }
                if (BlockProt.getDefaultConfig().disallowRedstoneOnPlace()) {
                    handler.setRedstone(false);
                }

                // So that other half's of doors lock properly.
                Bukkit.getScheduler().runTaskLater(
                    this.blockProt,
                    handler::applyToOtherContainer,
                    1
                );
            }
        }
    }
}
