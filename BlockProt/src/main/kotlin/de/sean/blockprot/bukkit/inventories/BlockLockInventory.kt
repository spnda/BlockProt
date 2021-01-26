package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit

object BlockLockInventory {
    val INVENTORY_NAME = Strings.getString("inventories.block_lock.name", "Block Lock")
    fun createInventory() = Bukkit.createInventory(null, 9 * 1, INVENTORY_NAME)
}
