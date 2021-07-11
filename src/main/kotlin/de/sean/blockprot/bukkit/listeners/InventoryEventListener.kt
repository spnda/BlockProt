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

import de.sean.blockprot.bukkit.BlockProt
import de.sean.blockprot.bukkit.inventories.*
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.BlockInventoryHolder

class InventoryEventListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val state = InventoryState.get(player.uniqueId)
        if (state != null) {
            // We have some sort of inventory state, so we'll
            // assume the player is currently in some of our inventories.
            when (event.inventory.holder) {
                is BlockLockInventory -> (event.inventory.holder as BlockLockInventory).onClick(event, state)
                is BlockInfoInventory -> (event.inventory.holder as BlockInfoInventory).onClick(event, state)
                is FriendDetailInventory -> (event.inventory.holder as FriendDetailInventory).onClick(event, state)
                is FriendManageInventory -> (event.inventory.holder as FriendManageInventory).onClick(event, state)
                is FriendSearchResultInventory -> (event.inventory.holder as FriendSearchResultInventory).onClick(event, state)
                is UserSettingsInventory -> (event.inventory.holder as UserSettingsInventory).onClick(event, state)
            }
        } else {
            // No state, let's check if they're in some block inventory.
            try {
                // Casting null does not trigger a ClassCastException.
                if (event.inventory.holder == null) return
                val blockHolder = event.inventory.holder as BlockInventoryHolder
                if (BlockProt.getDefaultConfig().isLockable(blockHolder.block.type)) {
                    // Ok, we have a lockable block, check if they can write anything to this.
                    // TODO: Implement a Cache for this lookup, it seems to be quite expensive.
                    //       We should probably use a MultiMap, or implement our own Key that
                    //       can use multiple key objects, a Block and Player in this case.
                    val handler = BlockNBTHandler(blockHolder.block)
                    val playerUuid = player.uniqueId.toString()
                    if (!handler.canAccess(playerUuid)) {
                        player.closeInventory()
                        event.isCancelled = true
                        return
                    }
                    val friend = handler.getFriend(playerUuid)
                    if (friend.isPresent && !friend.get().canWrite()) {
                        event.isCancelled = true
                        return
                    }
                }
            } catch (e: ClassCastException) {
                // It's not a block and it's therefore also not lockable.
                // This is probably some other custom inventory from another
                // plugin, or possibly some entity inventory, e.g. villagers.
                return
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        val state = InventoryState.get(player.uniqueId) ?: return
        when (event.inventory.holder) {
            is BlockLockInventory -> (event.inventory.holder as BlockLockInventory).onClose(event, state)
            is BlockInfoInventory -> (event.inventory.holder as BlockInfoInventory).onClose(event, state)
            is FriendDetailInventory -> (event.inventory.holder as FriendDetailInventory).onClose(event, state)
            is FriendManageInventory -> (event.inventory.holder as FriendManageInventory).onClose(event, state)
            is FriendSearchResultInventory -> (event.inventory.holder as FriendSearchResultInventory).onClose(event, state)
            is UserSettingsInventory -> (event.inventory.holder as UserSettingsInventory).onClose(event, state)
        }
    }
}
