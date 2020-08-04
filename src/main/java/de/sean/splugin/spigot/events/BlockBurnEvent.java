package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.util.SLockUtil;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.List;

/* Spigot */
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/* NBT API */
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTTileEntity;

public class BlockBurnEvent implements Listener {
    @EventHandler
    public static void BlockBurn(final org.bukkit.event.block.BlockBurnEvent event) {
        BlockState blockState = event.getBlock().getState();
        if (!(blockState instanceof Chest
            || blockState instanceof Barrel)) return;
        NBTTileEntity blockTileEntity = new NBTTileEntity(blockState);
        NBTCompound blockTile = blockTileEntity.getPersistentDataContainer();
        if (blockTile != null) {
            String nbt = blockTile.getString(SLockUtil.LOCK_ATTRIBUTE);
            List<String> access = SUtil.parseStringList(nbt);
            if (!access.isEmpty()) {
                // If the block is locked by any user, prevent it from burning down.
                event.setCancelled(true);
            }
        }
    }
}
