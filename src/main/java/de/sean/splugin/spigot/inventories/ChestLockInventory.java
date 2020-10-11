package de.sean.splugin.spigot.inventories;

import de.sean.splugin.util.SUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public class ChestLockInventory {
    public static final String INVENTORY_NAME = "Chest Lock";
    public static Inventory inventory = Bukkit.createInventory(null, 9, INVENTORY_NAME);
}
