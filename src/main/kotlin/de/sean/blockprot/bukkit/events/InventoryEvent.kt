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
        when (event.view.title) {
            BlockInfoInventory.inventoryName -> BlockInfoInventory.onInventoryClick(event, state)
            BlockLockInventory.inventoryName -> BlockLockInventory.onInventoryClick(event, state)
            FriendDetailInventory.INSTANCE.inventoryName -> FriendDetailInventory.INSTANCE.onInventoryClick(event, state)
            FriendManageInventory.INSTANCE.inventoryName -> FriendManageInventory.INSTANCE.onInventoryClick(event, state)
            FriendSearchResultInventory.inventoryName -> FriendSearchResultInventory.onInventoryClick(event, state)
            UserSettingsInventory.inventoryName -> UserSettingsInventory.onInventoryClick(event, state)
        }
    }
}
