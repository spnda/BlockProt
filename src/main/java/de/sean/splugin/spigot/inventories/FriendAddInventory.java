package de.sean.splugin.spigot.inventories;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class FriendAddInventory {
    public static final String INVENTORY_NAME = "Add Friend";
    /// The content needs to be added when opened.
    public static Inventory inventory = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME);
}
