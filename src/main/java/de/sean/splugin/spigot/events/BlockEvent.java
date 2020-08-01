package de.sean.splugin.spigot.events;

/* Spigot */
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SUtil;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

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
            if (!access.contains(event.getPlayer().getUniqueId().toString())) {
                // Prevent unauthorized players from breaking locked blocks.
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public static void PlayerBlockPlace(BlockPlaceEvent event) {
        
    }
}
