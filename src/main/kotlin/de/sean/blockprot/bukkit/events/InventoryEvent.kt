package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.inventories.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryEvent : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val state = InventoryState.get(player.uniqueId)
        when (event.inventory.holder) {
            is BlockLockInventory -> (event.inventory.holder as BlockLockInventory).onInventoryClick(event, state)
            is BlockInfoInventory -> (event.inventory.holder as BlockInfoInventory).onInventoryClick(event, state)
            is FriendDetailInventory -> (event.inventory.holder as FriendDetailInventory).onInventoryClick(event, state)
            is FriendManageInventory -> (event.inventory.holder as FriendManageInventory).onInventoryClick(event, state)
            is FriendSearchResultInventory -> (event.inventory.holder as FriendSearchResultInventory).onInventoryClick(event, state)
            is UserSettingsInventory -> (event.inventory.holder as UserSettingsInventory).onInventoryClick(event, state)
        }
    }
}
