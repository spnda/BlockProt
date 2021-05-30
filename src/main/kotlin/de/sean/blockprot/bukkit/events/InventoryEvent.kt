package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.inventories.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryEvent : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val state = InventoryState.get(player.uniqueId) ?: return
        when (event.inventory.holder) {
            is BlockLockInventory -> (event.inventory.holder as BlockLockInventory).onClick(event, state)
            is BlockInfoInventory -> (event.inventory.holder as BlockInfoInventory).onClick(event, state)
            is FriendDetailInventory -> (event.inventory.holder as FriendDetailInventory).onClick(event, state)
            is FriendManageInventory -> (event.inventory.holder as FriendManageInventory).onClick(event, state)
            is FriendSearchResultInventory -> (event.inventory.holder as FriendSearchResultInventory).onClick(event, state)
            is UserSettingsInventory -> (event.inventory.holder as UserSettingsInventory).onClick(event, state)
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
