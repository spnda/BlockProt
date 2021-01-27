package de.sean.blockprot.util

import de.sean.blockprot.BlockProt

object Strings {
    /* Constants */
    val PERMISSION_GRANTED = getMessage("permission_granted", "Permission granted.")
    val NO_PERMISSION = getMessage("no_permission", "No permission.")

    val UNLOCKED = getMessage("unlocked", "Unlocked.")

    val FRIEND_ADDED = getMessage("friend_added", "Friend added.")
    val FRIEND_ALREADY_ADDED = getMessage("friend_already_added", "Friend already added.")

    val FRIEND_REMOVED = getMessage("friend_removed", "Friend removed.")
    val FRIEND_CANT_BE_REMOVED = getMessage("friend_cant_be_removed", "Friend cannot be removed.")

    val REDSTONE_ADDED = getMessage("redstone_added", "Redstone protection added.")
    val REDSTONE_REMOVED = getMessage("redstone_removed", "Redstone protection removed.")

    // Inventories
    val LOCK = getString("inventories.block_lock.items.lock.lock", "Lock")
    val UNLOCK = getString("inventories.block_lock.items.lock.unlock", "Unlock")
    val BACK = getString("inventories.back", "Back")

    val BLOCK_LOCK_INFO = getString("inventories.block_lock.items.info", "Info")
    val BLOCK_LOCK_ADD_FRIENDS = getString("inventories.block_lock.items.add_friends", "Add Friends")
    val BLOCK_LOCK_REMOVE_FRIENDS = getString("inventories.block_lock.items.remove_friends", "Remove Friends")
    val BLOCK_LOCK_REDSTONE_ACTIVATE = getString("inventories.block_lock.items.redstone.activate", "Activate Redstone")
    val BLOCK_LOCK_REDSTONE_DEACTIVATE = getString("inventories.block_lock.items.redstone.deactivate", "Deactivate Redstone")

    // Permissions
    const val BLOCKPROT_LOCK = "blockprot.lock"
    const val BLOCKPROT_INFO = "blockprot.info"
    const val BLOCKPROT_ADMIN = "blockprot.admin"

    /**
     * Get a message from the config by string entry. If it cannot be found and
     * default is not null, default is returned
     */
    private fun getMessage(location: String, default: String?): String {
        val string = BlockProt.instance.config.getString("messages.$location.text")
        if (string == null || string.isEmpty())
            return default ?: ""
        return string
    }

    fun getString(location: String, default: String?): String {
        val string = BlockProt.instance.config.getString(location)
        if (string == null || string.isEmpty())
            return default ?: ""
        return string
    }
}
