package de.sean.blockprot.bukkit.inventories

import org.bukkit.Bukkit

object BlockInfoInventory {
    const val INVENTORY_NAME = "Block Info"
    var inventory = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)
}
