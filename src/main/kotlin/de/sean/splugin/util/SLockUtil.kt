package de.sean.splugin.util

import org.bukkit.block.Block
import java.util.HashMap

object SLockUtil {
    const val OWNER_ATTRIBUTE = "splugin_owner"
    const val LOCK_ATTRIBUTE = "splugin_lock"
    const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"
    val lock = HashMap<String, Block>()
}
