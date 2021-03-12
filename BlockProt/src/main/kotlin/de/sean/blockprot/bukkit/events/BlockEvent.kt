package de.sean.blockprot.bukkit.events

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.tasks.DoubleChestLocker
import de.tr7zw.nbtapi.NBTEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin

class BlockEvent(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        if (!LockUtil.isLockable(event.block.state)) return
        val handler = BlockLockHandler(event.block)
        // If the block is protected by any user, prevent it from burning down.
        if (handler.isProtected()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun playerBlockBreak(event: BlockBreakEvent) {
        if (!LockUtil.isLockable(event.block.state)) return // We only want to check for Tiles.
        val handler = BlockLockHandler(event.block)
        if (!handler.isOwner(event.player.uniqueId.toString()) && handler.isProtected()) {
            // Prevent unauthorized players from breaking locked blocks.
            event.isCancelled = true
        }
    }

    @EventHandler
    fun playerBlockPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val playerUuid = event.player.uniqueId.toString()
        when (block.type) {
            Material.CHEST -> {
                val handler = BlockLockHandler(block)

                // After placing, it takes 1 tick for the chests to connect.
                Bukkit.getScheduler().runTaskLater(plugin, DoubleChestLocker(handler, block, event.player) { allowed ->
                    if (!allowed) {
                        // We can't cancel the event 1 tick later, its already executed. We'll just need to destroy the block and drop it.
                        val location = block.location
                        event.player.world.getBlockAt(location).breakNaturally() // Let it break and drop itself
                    }
                }, 1)

                val nbtEntity = NBTEntity(event.player).persistentDataContainer
                if (nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE) != false) {
                    handler.lockBlock(event.player, event.player.isOp, null)
                }
            }
            // We won't lock normal blocks on placing.
            in LockUtil.lockableTileEntities -> {
                val nbtEntity = NBTEntity(event.player).persistentDataContainer
                // Assign a empty string for no owner to not have NPEs when reading
                print(nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE))
                BlockLockHandler(block).setOwner(
                    if (nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE) != false) playerUuid else ""
                )
            }
            else -> return
        }
    }
}
