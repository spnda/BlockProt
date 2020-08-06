package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.ArrayList;
import java.util.List;

/* Spigot */
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/* NBT API */
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.gradle.internal.impldep.org.eclipse.jgit.util.NB;

public class BlockEvent implements Listener {
    @EventHandler
    public static void PlayerBlockBreak(BlockBreakEvent event) {
        BlockState chestState = event.getBlock().getState();
        if (!(chestState instanceof Chest)) return;
        NBTTileEntity blockTileEntity = new NBTTileEntity(chestState);
        NBTCompound blockTile = blockTileEntity.getPersistentDataContainer();
        if (blockTile != null) {
            String nbt = blockTile.getString(SLockUtil.LOCK_ATTRIBUTE);
            List<String> access = SUtil.parseStringList(nbt);
            if (!access.isEmpty() && !access.contains(event.getPlayer().getUniqueId().toString())) {
                // Prevent unauthorized players from breaking locked blocks.
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void PlayerBlockPlace(BlockPlaceEvent event) {
        FileConfiguration config = App.getInstance().getConfig();
        // If no setting has been set, the config will return false, as it is the default value for a primitive boolean.
        // As we want the automatic locking to be default, we will instead have a setting to not lock a block when placed.
        boolean notLockOnPlace = config.getBoolean("Players." + event.getPlayer().getUniqueId() + ".LockOnPlace");
        Block block = event.getBlockPlaced();
        String uuid = event.getPlayer().getUniqueId().toString();
        switch (block.getType()) {
            case CHEST:
                Location location = event.getBlockPlaced().getLocation();
                Chest chest = (Chest)block.getState();
                if (chest.getBlockInventory().getHolder() instanceof DoubleChest) {
                    // This is going to be a double chest.
                    // If the other chest it is connecting too is locked towards the placer,
                    // prevent the placement.
                    DoubleChest doubleChest = (DoubleChest)chest.getBlockInventory().getHolder();
                    Location secChest = doubleChest.getLocation();
                    // If we are targeting the further away chest block, get the closer one
                    // (Closer/Further away from 0, 0, 0)
                    if (location.getX() > secChest.getX()) secChest.subtract(.5, 0, 0);
                    else if (location.getZ() > secChest.getZ()) secChest.subtract(0, 0, .5);
                    else secChest.add(.5, 0, .5);
                    BlockState secChestState = event.getPlayer().getWorld().getBlockAt(secChest).getState();
                    NBTTileEntity secTileEntity = new NBTTileEntity(secChestState);

                    NBTCompound secTile = secTileEntity.getPersistentDataContainer();
                    List<String> secNBT = SUtil.parseStringList(secTile.getString(SLockUtil.LOCK_ATTRIBUTE));
                    if (secNBT.contains(uuid)) {
                        // The player placing the new chest has access to the to other chest.
                        NBTCompound newChestNBT = new NBTTileEntity(block.getState()).getPersistentDataContainer();
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
                if (!notLockOnPlace) {
                    NBTTileEntity blockTileEntity = new NBTTileEntity(block.getState());
                    NBTCompound nbt = blockTileEntity.getPersistentDataContainer();

                    List<String> nbtData = new ArrayList<>();
                    nbtData.add(uuid);
                    nbt.setString(SLockUtil.LOCK_ATTRIBUTE, nbtData.toString());
                }
                break;
            default:
                break;
        }
    }
}
