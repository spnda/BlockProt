package de.sean.blockprot.bukkit.tasks

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.util.LockUtil.getDoubleChest
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.function.Consumer

class DoubleChestLocker(
    private val newHandler: BlockLockHandler,
    val block: Block,
    private val player: Player,
    private val callback: Consumer<Boolean>
) : Runnable {
    override fun run() {
        val doubleChest = getDoubleChest(block, player.world)
        if (doubleChest == null) {
            callback.accept(true)
            return
        }
        val oldChestHandler = BlockLockHandler(doubleChest.block)
        if (oldChestHandler.isProtected() && oldChestHandler.getOwner() != player.uniqueId.toString()) {
            callback.accept(false)
        } else {
            newHandler.setOwner(oldChestHandler.getOwner())
            newHandler.setAccess(oldChestHandler.getAccess())
            newHandler.setRedstone(oldChestHandler.getRedstone())
            callback.accept(true)
        }
    }
}
