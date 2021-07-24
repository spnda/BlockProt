package de.sean.blockprot.bukkit.listeners;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class HopperEventListener implements Listener {
    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() == null) return;
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getSource().getHolder())) return;
        if (event.getDestination().getType() == InventoryType.HOPPER) {
            // This is a hopper trying to pull from something.
            if (BlockProt.getDefaultConfig().isLockableInventory(event.getSource().getType())) {
                Block source = getBlock(event.getSource().getHolder());
                if (source != null) {
                    if (!(new BlockNBTHandler(source)).getRedstone()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * Get the block from a InventoryHolder, instead of just getting the block by location as before.
     */
    @Nullable
    private Block getBlock(InventoryHolder holder) {
        if (holder instanceof Chest) return ((Chest) holder).getBlock();
        else if (holder instanceof Furnace) return ((Furnace) holder).getBlock();
        else if (holder instanceof Hopper) return ((Hopper) holder).getBlock();
        else if (holder instanceof Barrel) return ((Barrel) holder).getBlock();
        else if (holder instanceof BrewingStand) return ((BrewingStand) holder).getBlock();
        else if (holder instanceof ShulkerBox) return ((ShulkerBox) holder).getBlock();
        else if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            if (doubleChest.getWorld() == null) return null;
            return doubleChest.getWorld().getBlockAt(doubleChest.getLocation());
        } else return null;
    }
}
