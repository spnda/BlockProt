package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.util.SLockUtil;

/* Spigot */
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;

/* NBT-API */
import de.tr7zw.nbtapi.NBTTileEntity;

public class HopperEvent implements Listener {
    @EventHandler
    public void onItemMove(final InventoryMoveItemEvent event) {
        if (event.getDestination().getType() == InventoryType.HOPPER) {
            // This is a hopper trying to pull from something.
            switch (event.getSource().getType()) {
                case CHEST:
                case FURNACE:
                case BARREL:
                case SHULKER_BOX:
                    // This hopper is trying to pull from some inventory which *may* be locked.
                    // Note: we do not have to check for double chests, as both sides of a chest are individually locked.
                    final Location sourceLocation = event.getSource().getLocation();
                    if (sourceLocation == null) break;
                    final NBTTileEntity source = new NBTTileEntity(sourceLocation.getBlock().getState());
                    if (!source.getBoolean(SLockUtil.REDSTONE_ATTRIBUTE)) event.setCancelled(true);
                    break;
            }
        }
    }
}
