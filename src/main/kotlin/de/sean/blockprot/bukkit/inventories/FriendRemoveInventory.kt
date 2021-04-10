package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit

object FriendRemoveInventory {
    val INVENTORY_NAME = Strings.getString("inventories.remove_friend.name", "Remove Friend")
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)
}
