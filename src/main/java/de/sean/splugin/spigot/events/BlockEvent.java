package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.SPlugin;
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.ArrayList;
import java.util.List;

/* Spigot */
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/* NBT API */
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;

public class BlockEvent implements Listener {
    @EventHandler
    public static void BlockBurn(final BlockBurnEvent event) {
        final BlockState blockState = event.getBlock().getState();
        if (!(blockState instanceof Chest
                || blockState instanceof Barrel)) return;
        final NBTCompound blockTile = new NBTTileEntity(blockState).getPersistentDataContainer();
        if (blockTile != null) {
            final List<String> access = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));
            if (!access.isEmpty()) {
                // If the block is locked by any user, prevent it from burning down.
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void PlayerBlockBreak(final BlockBreakEvent event) {
        final BlockState chestState = event.getBlock().getState();
        if (!(chestState instanceof Chest)) return;
        final NBTCompound blockTile = new NBTTileEntity(chestState).getPersistentDataContainer();
        if (blockTile != null) {
            final List<String> access = SUtil.parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE));
            if (!access.isEmpty() && !access.contains(event.getPlayer().getUniqueId().toString())) {
                // Prevent unauthorized players from breaking locked blocks.
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void PlayerBlockPlace(final BlockPlaceEvent event) {
        final FileConfiguration config = SPlugin.instance.getConfig();
        final Block block = event.getBlockPlaced();
        final String uuid = event.getPlayer().getUniqueId().toString();
        switch (block.getType()) {
            case CHEST:
                final Chest chest = (Chest)block.getState();
                if (chest.getBlockInventory().getHolder() instanceof DoubleChest) {
                    // This is going to be a double chest.
                    // If the other chest it is connecting too is locked towards the placer,
                    // prevent the placement.
                    final Location location = event.getBlockPlaced().getLocation();
                    final Location secChest = ((DoubleChest)chest.getBlockInventory().getHolder()).getLocation();
                    // If we are targeting the further away chest block, get the closer one
                    // (Closer/Further away from 0, 0, 0)
                    if (location.getX() > secChest.getX()) secChest.subtract(.5, 0, 0);
                    else if (location.getZ() > secChest.getZ()) secChest.subtract(0, 0, .5);
                    else secChest.add(.5, 0, .5);

                    final NBTCompound secTile = new NBTTileEntity(event.getPlayer().getWorld().getBlockAt(secChest).getState()).getPersistentDataContainer();
                    final List<String> secNBT = SUtil.parseStringList(secTile.getString(SLockUtil.LOCK_ATTRIBUTE));
                    if (secNBT.contains(uuid)) {
                        // The player placing the new chest has access to the to other chest.
                        final NBTCompound newChestNBT = new NBTTileEntity(block.getState()).getPersistentDataContainer();
                        newChestNBT.setString(SLockUtil.OWNER_ATTRIBUTE, secNBT.toString());
                        newChestNBT.setString(SLockUtil.LOCK_ATTRIBUTE, secNBT.toString());
                    } else {
                        // The player is trying to place a chest adjacent to a chest locked by another player.
                        event.setCancelled(true);
                    }
                    break;
                }
                // This is going to be a single chest.
                // We will fallthrough to the next case.
            case FURNACE:
            case HOPPER:
            case BARREL:
            case SHULKER_BOX:
                // If no setting has been set, the config will return false, as it is the default value for a primitive boolean.
                // As we want the automatic locking to be default, we will instead have a setting to not lock a block when placed.
                if (!config.getBoolean("players." + event.getPlayer().getUniqueId() + ".lockOnPlace")) {
                    new NBTTileEntity(block.getState()).getPersistentDataContainer().setString(SLockUtil.OWNER_ATTRIBUTE, uuid);
                }
                break;
        }
    }
}
