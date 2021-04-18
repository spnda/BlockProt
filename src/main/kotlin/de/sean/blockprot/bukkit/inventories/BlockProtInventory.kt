package de.sean.blockprot.bukkit.inventories

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

interface BlockProtInventory {
    val size: Int
    val inventoryName: String
    fun createInventory(): Inventory = Bukkit.createInventory(null, size, inventoryName)
}
