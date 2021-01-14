package de.sean.splugin.bukkit.events

import de.sean.splugin.bukkit.nbt.BlockLockHandler
import de.sean.splugin.bukkit.nbt.LockUtil
import de.sean.splugin.bukkit.tasks.DoubleChestLocker
import de.sean.splugin.util.ItemUtil
import de.sean.splugin.util.PluginConfig
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.plugin.java.JavaPlugin

class BlockEvent(val plugin: JavaPlugin) : Listener {
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
        if (blockState !is TileState) return // We only want to check for Tiles.
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

                // After placing, it takes 1 tick for the chests to connect.
                Bukkit.getScheduler().runTaskLater(plugin, DoubleChestLocker(handler, block, event.player) { allowed ->
                    if (!allowed) {
                        // We can't cancel the event 1 tick later, its already executed. We'll just need to destroy the block and drop it.
                        val location = event.blockPlaced.location
                        event.player.world.getBlockAt(location).breakNaturally() // Let it break and drop itself
                    }
                }, 1);

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