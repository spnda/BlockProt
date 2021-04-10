package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit

object BlockInfoInventory {
    val INVENTORY_NAME = Strings.getString("inventories.block_info.name", "Block Info")
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)
}
