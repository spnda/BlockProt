package de.sean.splugin.spigot.events

import de.sean.splugin.SPlugin
import de.sean.splugin.util.SLockUtil
import de.sean.splugin.util.SUtil.parseStringList
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
        if (!(blockState is Chest
                        || blockState is Barrel)) return
        val blockTile = NBTTileEntity(blockState).persistentDataContainer
        if (blockTile != null) {
            val access = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE))
            if (access.isNotEmpty()) {
                // If the block is locked by any user, prevent it from burning down.
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun playerBlockBreak(event: BlockBreakEvent) {
        val chestState = event.block.state as? Chest ?: return
        val blockTile = NBTTileEntity(chestState).persistentDataContainer
        if (blockTile != null) {
            val access = parseStringList(blockTile.getString(SLockUtil.LOCK_ATTRIBUTE))
            if (access.isNotEmpty() && !access.contains(event.player.uniqueId.toString())) {
                // Prevent unauthorized players from breaking locked blocks.
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun playerBlockPlace(event: BlockPlaceEvent) {
        val config = SPlugin.instance.config
        val block = event.blockPlaced
        val uuid = event.player.uniqueId.toString()
        when (block.type) {
            Material.CHEST -> {
                val chest = block.state as Chest
                if (chest.blockInventory.holder is DoubleChest) {
                    // This is going to be a double chest.
                    // If the other chest it is connecting too is locked towards the placer,
                    // prevent the placement.
                    val location = event.blockPlaced.location
                    val secChest = (chest.blockInventory.holder as DoubleChest?)!!.location
                    // If we are targeting the further away chest block, get the closer one
                    // (Closer/Further away from 0, 0, 0)
                    if (location.x > secChest.x) secChest.subtract(.5, 0.0, 0.0) else if (location.z > secChest.z) secChest.subtract(0.0, 0.0, .5) else secChest.add(.5, 0.0, .5)
                    val secTile = NBTTileEntity(event.player.world.getBlockAt(secChest).state).persistentDataContainer
                    val secNBT = parseStringList(secTile.getString(SLockUtil.LOCK_ATTRIBUTE))
                    if (secNBT.contains(uuid)) {
                        // The player placing the new chest has access to the to other chest.
                        val newChestNBT = NBTTileEntity(block.state).persistentDataContainer
                        newChestNBT.setString(SLockUtil.OWNER_ATTRIBUTE, secNBT.toString())
                        newChestNBT.setString(SLockUtil.LOCK_ATTRIBUTE, secNBT.toString())
                    } else {
                        // The player is trying to place a chest adjacent to a chest locked by another player.
                        event.isCancelled = true
                    }
                    return
                }
                // If no setting has been set, the config will return false, as it is the default value for a primitive boolean.
                // As we want the automatic locking to be default, we will instead have a setting to not lock a block when placed.
                if (!config.getBoolean("players." + event.player.uniqueId + ".lockOnPlace")) {
                    NBTTileEntity(block.state).persistentDataContainer.setString(SLockUtil.OWNER_ATTRIBUTE, uuid)
                }
            }
            Material.FURNACE, Material.HOPPER, Material.BARREL, Material.SHULKER_BOX -> if (!config.getBoolean("players." + event.player.uniqueId + ".lockOnPlace")) {
                NBTTileEntity(block.state).persistentDataContainer.setString(SLockUtil.OWNER_ATTRIBUTE, uuid)
            } else NBTTileEntity(block.state).persistentDataContainer.setString(SLockUtil.OWNER_ATTRIBUTE, "") // Assign a empty string to not have NPEs when reading
            else -> return
        }
    }
}
