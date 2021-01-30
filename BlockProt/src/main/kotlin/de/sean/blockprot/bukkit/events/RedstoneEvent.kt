package de.sean.blockprot.bukkit.events

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent
import java.lang.RuntimeException

class RedstoneEvent : Listener {
    @EventHandler
    fun onRedstone(event: BlockRedstoneEvent) {
        // If this is a lockable block and the redstone protection is activated, set the redstone to 0
        if (LockUtil.isLockableBlock(event.block.state) && BlockLockHandler(event.block).isRedstoneProtected()) {
            event.newCurrent = 0
        }
    }
}
