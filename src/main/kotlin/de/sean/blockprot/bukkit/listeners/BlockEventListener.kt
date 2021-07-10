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
package de.sean.blockprot.bukkit.listeners

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.events.BlockLockOnPlaceEvent
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler
import de.sean.blockprot.bukkit.tasks.DoubleChestLocker
import de.tr7zw.changeme.nbtapi.NBTItem
import de.tr7zw.changeme.nbtapi.NBTTileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin

class BlockEventListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.block.world)) return
        if (!BlockProt.getDefaultConfig().isLockable(event.block.type)) return
        // The event hasn't been cancelled already, we'll check if need
        // to cancel it manually.
        val handler = BlockNBTHandler(event.block)
        // If the block is protected by any user, prevent it from burning down.
        if (handler.isProtected) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun playerBlockBreak(event: BlockBreakEvent) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.block.world)) return
        if (!BlockProt.getDefaultConfig().isLockable(event.block.type)) return // We only want to check for Tiles.

        val handler = BlockNBTHandler(event.block)
        if (!handler.isOwner(event.player.uniqueId.toString()) && handler.isProtected) {
            // Prevent unauthorized players from breaking locked blocks.
            event.isCancelled = true
        } else if (BlockProt.getDefaultConfig().isLockableShulkerBox(event.block.type) && event.isDropItems) {
            // The player can break the block. We will now check if its a shulker box,
            // so we can add NBT to the shulker box that it gets locked upon placing again.
            event.isDropItems = false // Prevent the event from dropping items itself
            val itemsToDrop = event.block.drops.first() // Shulker blocks should only have a single drop anyway
            val nbtTile = NBTTileEntity(event.block.state).persistentDataContainer
            val nbtItem = NBTItem(itemsToDrop, true)
            nbtItem.getOrCreateCompound("BlockEntityTag").getOrCreateCompound("PublicBukkitValues").mergeCompound(nbtTile)
            event.player.world.dropItemNaturally(event.block.state.location, itemsToDrop)
        }
    }

    @EventHandler
    fun playerBlockPlace(event: BlockPlaceEvent) {
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.block.world)) return
        if (!event.player.hasPermission(BlockNBTHandler.PERMISSION_LOCK)) return
        val block = event.blockPlaced
        val playerUuid = event.player.uniqueId.toString()
        when {
            block.type == Material.CHEST -> {
                val handler = BlockNBTHandler(block)

                // After placing, it takes 1 tick for the chests to connect.
                Bukkit.getScheduler().runTaskLater(
                    plugin,
                    DoubleChestLocker(handler, block, event.player) { allowed ->
                        if (!allowed) {
                            // We can't cancel the event 1 tick later, its already executed. We'll just need to destroy the block and drop it.
                            val location = block.location
                            event.player.world.getBlockAt(location).breakNaturally() // Let it break and drop itself
                        }
                    },
                    1
                )

                if (PlayerSettingsHandler(event.player).lockOnPlace) {
                    val lockOnPlaceEvent = BlockLockOnPlaceEvent(event.block, event.player)
                    Bukkit.getPluginManager().callEvent(lockOnPlaceEvent)
                    if (!lockOnPlaceEvent.isCancelled) {
                        handler.lockBlock(event.player, null)
                        val settingsHandler = PlayerSettingsHandler(event.player)
                        val friends = settingsHandler.defaultFriends
                        for (friend in friends) {
                            handler.addFriend(friend)
                        }
                    }

                    if (BlockProt.getDefaultConfig().disallowRedstoneOnPlace()) {
                        handler.redstone = false
                    }
                }
            }
            BlockProt.getDefaultConfig().isLockableTileEntity(event.block.type) || BlockProt.getDefaultConfig().isLockableBlock(event.block.type) -> {
                val handler = BlockNBTHandler(block)
                // We only try to lock the block if it isn't locked already.
                // Shulker boxes might already be locked, from previous placing.
                if (handler.isNotProtected) {
                    val lockOnPlaceEvent = BlockLockOnPlaceEvent(event.block, event.player)
                    Bukkit.getPluginManager().callEvent(lockOnPlaceEvent)
                    if (!lockOnPlaceEvent.isCancelled) {
                        // Assign a empty string for no owner to not have NPEs when reading
                        handler.owner =
                            if (PlayerSettingsHandler(event.player).lockOnPlace) playerUuid
                            else ""
                    }
                    if (BlockProt.getDefaultConfig().disallowRedstoneOnPlace()) {
                        handler.redstone = false
                    }
                }
            }
            else -> return
        }
    }
}
