package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.nbt.BlockLockHandler
import de.sean.splugin.bukkit.nbt.LockUtil
import de.sean.splugin.util.PluginConfig
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockEvent : Listener {
    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        val blockState = event.block.state
        if (!(blockState is Chest || blockState is Barrel)) return
        val handler = BlockLockHandler(NBTTileEntity(blockState))
        // If the block is protected by any user, prevent it from burning down.
        if (handler.isProtected()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun playerBlockBreak(event: BlockBreakEvent) {
        val blockState = event.block.state
        val handler = BlockLockHandler(NBTTileEntity(blockState))
        if (!handler.isOwner(event.player.uniqueId.toString()) && handler.isProtected()) {
            // Prevent unauthorized players from breaking locked blocks.
            event.isCancelled = true
        }
    }

    @EventHandler
    fun playerBlockPlace(event: BlockPlaceEvent) {
        val config = PluginConfig.instance.configuration
        val block = event.blockPlaced
        val uuid = event.player.uniqueId.toString()
        when (block.type) {
            Material.CHEST -> {
                val handler = BlockLockHandler(NBTTileEntity(block.state))
                val chest = block.state as Chest
                if (chest.blockInventory.holder is DoubleChest) {
                    // This is going to be a double chest.
                    // If the other chest it is connecting too is locked towards the placer,
                    // prevent the placement.
                    val second = (chest.blockInventory.holder as DoubleChest?)!!.location
                    // If we are targeting the further away chest block, get the closer one
                    // (Closer/Further away from 0, 0, 0)
                    when {
                        block.x > second.x -> second.subtract(.5, 0.0, 0.0)
                        block.z > second.z -> second.subtract(0.0, 0.0, .5)
                        else -> second.add(.5, 0.0, .5)
                    }
                    val newHandler = BlockLockHandler(NBTTileEntity(event.player.world.getBlockAt(second).state))
                    if (newHandler.isOwner(uuid)) {
                        // The player placing the new chest has access to the to other chest.
                        handler.setOwner(uuid)
                        handler.setAccess(newHandler.getAccess())
                        handler.setRedstone(newHandler.getRedstone())
                    } else {
                        // The player is trying to place a chest adjacent to a chest locked by another player.
                        event.isCancelled = true
                    }
                    return
                }

                if (!config.getBoolean("players." + event.player.uniqueId + ".lockOnPlace")) {
                    handler.lockBlock(event.player.uniqueId.toString(), event.player.isOp, null)
                }
            }
            in LockUtil.lockableBlocks -> if (!config.getBoolean("players." + event.player.uniqueId + ".lockOnPlace")) {
                BlockLockHandler(NBTTileEntity(block.state)).setOwner(uuid)
            } else BlockLockHandler(NBTTileEntity(block.state)).setOwner("") // Assign a empty string to not have NPEs when reading
            else -> return
        }
    }
}
