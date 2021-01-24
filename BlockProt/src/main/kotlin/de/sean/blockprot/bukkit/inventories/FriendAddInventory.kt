package de.sean.blockprot.bukkit.inventories

import org.bukkit.Bukkit

object FriendAddInventory {
    const val INVENTORY_NAME = "Add Friend"
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, FriendAddInventory.INVENTORY_NAME)
}
