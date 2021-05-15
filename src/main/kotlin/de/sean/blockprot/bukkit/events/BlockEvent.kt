package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.parseStringList
import de.sean.blockprot.bukkit.tasks.DoubleChestLocker
import de.tr7zw.nbtapi.NBTEntity
import de.tr7zw.nbtapi.NBTItem
import de.tr7zw.nbtapi.NBTTileEntity
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
        if (LockUtil.shulkerBoxes.contains(event.block.state.type) && event.isDropItems) {
            event.isDropItems = false // Prevent the event from dropping items itself
            val itemsToDrop = event.block.drops.first() // Shulker blocks should only have a single drop anyway
            val nbtTile = NBTTileEntity(event.block.state).persistentDataContainer
            val nbtItem = NBTItem(itemsToDrop, true)
            nbtItem.getOrCreateCompound("BlockEntityTag").getOrCreateCompound("PublicBukkitValues").mergeCompound(nbtTile)
            event.player.world.dropItemNaturally(event.block.state.location, itemsToDrop)
        } else {
            val handler = BlockLockHandler(event.block)
            if (!handler.isOwner(event.player.uniqueId.toString()) && handler.isProtected()) {
                // Prevent unauthorized players from breaking locked blocks.
                event.isCancelled = true
            }
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
                Bukkit.getScheduler().runTaskLater(
                    plugin,
                    DoubleChestLocker(handler, block, event.player) { allowed ->
                        if (!allowed) {
                            // We can't cancel the event 1 tick later, its already executed. We'll just need to destroy the block and drop it.
                            val location = block.location
                            event.player.world.getBlockAt(location).breakNaturally() // Let it break and drop itself
                        }
                    },
                    1
                )

                if (LockUtil.shouldLockOnPlace(event.player)) {
                    handler.lockBlock(event.player, event.player.isOp, null)
                    val nbtEntity = NBTEntity(event.player).persistentDataContainer
                    val friends = parseStringList(nbtEntity.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
                    handler.setAccess(friends)
                    if (LockUtil.disallowRedstoneOnPlace()) {
                        handler.setRedstone(redstone = false)
                    }
                }
            }
            // We won't lock normal blocks on placing.
            in LockUtil.lockableTileEntities, in LockUtil.lockableBlocks -> {
                val handler = BlockLockHandler(block)
                // We only try to lock the block if it isn't locked already.
                // Shulker boxes might already be locked, from previous placing.
                if (handler.isNotProtected()) {
                    // Assign a empty string for no owner to not have NPEs when reading
                    handler.setOwner(
                        if (LockUtil.shouldLockOnPlace(event.player)) playerUuid else ""
                    )
                    if (LockUtil.disallowRedstoneOnPlace()) {
                        handler.setRedstone(redstone = false)
                    }
                }
            }
            else -> return
        }
    }
}
