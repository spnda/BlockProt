package de.sean.blockprot.bukkit.inventories

import org.bukkit.Bukkit

object BlockLockInventory {
    const val INVENTORY_NAME = "Block Lock"
    var inventory = Bukkit.createInventory(null, 9, INVENTORY_NAME)
}
