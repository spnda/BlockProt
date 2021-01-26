package de.sean.blockprot.bukkit.tasks

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory
import java.util.function.Consumer

class DoubleChestLocker(private val newHandler: BlockLockHandler, val block: Block, private val player: Player, private val callback: Consumer<Boolean>) : Runnable {
    override fun run() {
        val doubleChest = getDoubleChest()
        if (doubleChest == null) {
            callback.accept(true)
            return
        }
        val oldChestHandler = BlockLockHandler(NBTTileEntity(doubleChest))
        if (oldChestHandler.isProtected() && oldChestHandler.getOwner() != player.uniqueId.toString()) {
            callback.accept(false)
        } else {
            newHandler.setOwner(oldChestHandler.getOwner())
            newHandler.setAccess(oldChestHandler.getAccess())
            newHandler.setRedstone(oldChestHandler.getRedstone())
            callback.accept(true)
        }
    }

    private fun getDoubleChest(): BlockState? {
        var doubleChest: DoubleChest? = null
        val chestState = block.state
        if (chestState is Chest) {
            val inventory = chestState.inventory
            if (inventory is DoubleChestInventory) {
                doubleChest = inventory.holder
            }
        }
        if (doubleChest == null) return null
        val second = doubleChest.location

        when {
            block.x > second.x -> second.subtract(.5, 0.0, 0.0)
            block.z > second.z -> second.subtract(0.0, 0.0, .5)
            else -> second.add(.5, 0.0, .5)
        }

        return player.world.getBlockAt(second).state
    }
}
