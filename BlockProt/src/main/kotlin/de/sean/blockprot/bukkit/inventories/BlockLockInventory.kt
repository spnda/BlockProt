package de.sean.blockprot.bukkit.inventories

import org.bukkit.Bukkit

object BlockLockInventory {
    const val INVENTORY_NAME = "Block Lock"
    fun createInventory() = Bukkit.createInventory(null, 9 * 1, BlockLockInventory.INVENTORY_NAME)
}
