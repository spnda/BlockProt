package de.sean.splugin.spigot.events

import de.sean.splugin.util.SLockUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityExplodeEvent

class ExplodeEvent : Listener {
    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.MINECART_TNT || e.entityType == EntityType.PRIMED_TNT) {
            val it = e.blockList().iterator()
            while (it.hasNext()) {
                val b = it.next()
                when (b.type) {
                    Material.CHEST, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.HOPPER, Material.BARREL,
                    Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX
                    -> {
                        // Someone owns this block, block its destroying.
                        if (NBTTileEntity(b.state).getStringList(SLockUtil.LOCK_ATTRIBUTE) != null) it.remove()
                    }
                    else -> break
                }
            }
        } else if (e.entityType == EntityType.CREEPER) {
            // We don't want mob griefing but villagers use mob griefing to work
            // So we'll just prevent creepers from destroying blocks like this.
            e.isCancelled = true
        }
    }
}
