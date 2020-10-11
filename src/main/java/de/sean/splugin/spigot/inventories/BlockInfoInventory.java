package de.sean.splugin.spigot.inventories;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

// UNUSED
public class BlockInfoInventory {
    public static final String INVENTORY_NAME = "Block Info";
    public static Inventory inventory = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME);
}
