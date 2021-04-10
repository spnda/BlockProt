package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit

object FriendAddInventory {
    val INVENTORY_NAME = Strings.getString("inventories.add_friend.name", "Add Friend")
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)
}
