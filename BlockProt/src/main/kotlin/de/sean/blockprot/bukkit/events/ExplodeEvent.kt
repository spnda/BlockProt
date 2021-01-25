package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import java.util.logging.Logger

class ExplodeEvent : Listener {
    companion object {
        // All Entities and Blocks taken from here
        // https://minecraft.gamepedia.com/Explosion
        /* val explosiveEntities: List<EntityType> = mutableListOf(
            EntityType.WITHER, EntityType.ENDER_CRYSTAL, EntityType.CREEPER, EntityType.PRIMED_TNT, EntityType.MINECART_TNT, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.WITHER_SKULL
        )
        val explosiveBlocks = mutableListOf(
            Material.RESPAWN_ANCHOR,
            Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED, Material.LIGHT_BLUE_BED, Material.YELLOW_BED, Material.LIME_BED, Material.PINK_BED, Material.GRAY_BED, Material.LIGHT_GRAY_BED, Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED, Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED,
        ) */
    }

    @EventHandler
    fun onBlockExplode(e: BlockExplodeEvent) {
        Logger.getLogger(this.javaClass.simpleName).info(e.block.type.toString())
        // BlockExplodeEvent happens *after* the block has exploded
        checkBlocks(e.blockList().iterator())
    }

    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        if (e.entityType == EntityType.CREEPER) {
            // We don't want mob griefing but villagers use mob griefing to work
            // So we'll just prevent creepers from destroying blocks like this.
            e.isCancelled = true;
        } else {
            checkBlocks(e.blockList().iterator())
        }
    }

    private fun checkBlocks(it: MutableIterator<Block>) {
        while (it.hasNext()) {
            val b = it.next()
            when (b.type) {
                in LockUtil.lockableBlocks -> {
                    // Someone owns this block, block its destroying.
                    val handler = BlockLockHandler(NBTTileEntity(b.state))
                    if (handler.isProtected()) it.remove()
                }
                // adding a break here affects the while loop causing it to only check one block
                else -> {}
            }
        }
    }
}
