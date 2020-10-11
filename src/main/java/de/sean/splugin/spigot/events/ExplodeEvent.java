package de.sean.splugin.spigot.events;

import de.sean.splugin.util.SLockUtil;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class ExplodeEvent implements Listener {
    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent e) {
        if (e.getEntityType() == EntityType.MINECART_TNT || e.getEntityType() == EntityType.PRIMED_TNT) {
            final Iterator<Block> it = e.blockList().iterator();
            while (it.hasNext()) {
                final Block b = it.next();
                if (b.getType() == Material.CHEST) {
                    // Someone owns this chest, block its destroying.
                    if (new NBTTileEntity(b.getState()).getStringList(SLockUtil.LOCK_ATTRIBUTE) != null) it.remove();
                }
            }
        } else if (e.getEntityType() == EntityType.CREEPER) {
            // We don't want mob griefing but villagers use mob griefing to work
            // So we'll just prevent creepers from destroying blocks like this.
            e.setCancelled(true);
        }
    }
}
