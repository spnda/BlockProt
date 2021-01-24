package de.sean.blockprot.bukkit.inventories

import org.bukkit.Bukkit

object FriendRemoveInventory {
    const val INVENTORY_NAME = "Remove Friend"
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, FriendRemoveInventory.INVENTORY_NAME)
}
