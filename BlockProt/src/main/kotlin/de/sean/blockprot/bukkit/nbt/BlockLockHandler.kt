package de.sean.blockprot.bukkit.nbt

import de.tr7zw.nbtapi.NBTTileEntity
import java.util.ArrayList

class BlockLockHandler constructor(entity: NBTTileEntity) {
    companion object {
        const val OWNER_ATTRIBUTE = "splugin_owner"
        const val LOCK_ATTRIBUTE = "splugin_lock"
        const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"
    }

    private val container = entity.persistentDataContainer

    private fun parseStringList(str: String): List<String> {
        val ret: MutableList<String> = ArrayList(listOf(*str.replace("^\\[|]$".toRegex(), "").split(", ").toTypedArray()))
        ret.removeIf { obj: String -> obj.isEmpty() }
        return ret
    }

    fun getOwner(): String = container.getString(OWNER_ATTRIBUTE) ?: ""
    fun getAccess() = parseStringList(container.getString(LOCK_ATTRIBUTE))
    fun getRedstone(): Boolean = container.getBoolean(REDSTONE_ATTRIBUTE)

    fun setOwner(string: String) = container.setString(OWNER_ATTRIBUTE, string)
    fun setAccess(list: List<String>) = container.setString(LOCK_ATTRIBUTE, list.toString())
    fun setRedstone(redstone: Boolean) = container.setBoolean(REDSTONE_ATTRIBUTE, redstone)

    fun isNotProtected() = getOwner().isEmpty() && getAccess().isEmpty()
    fun isProtected() = !isNotProtected()
    fun isRedstoneProtected(): Boolean = getRedstone()

    fun isOwner(player: String) = getOwner() == player
    fun canAccess(player: String) = if (isProtected()) (getOwner() == player || getAccess().contains(player)) else getOwner().isEmpty()

    fun lockBlock(player: String, isOp: Boolean, doubleChest: NBTTileEntity?): LockReturnValue {
        var owner = container.getString(OWNER_ATTRIBUTE) ?: ""
        if (owner.isEmpty()) {
            // This block is not owned by anyone, this user can claim this block
            owner = player
            container.setString(OWNER_ATTRIBUTE, owner)
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, owner)
            return LockReturnValue(true, "Permission granted.")
        } else if ((owner == player) || (isOp && owner.isNotEmpty())) {
            container.setString(OWNER_ATTRIBUTE, "")
            container.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, "")
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            return LockReturnValue(true, "Unlocked.")
        }
        return LockReturnValue(false, "No permission.")
    }

    fun lockRedstoneForBlock(player: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        if (owner == player) {
            val redstone = container.getBoolean(REDSTONE_ATTRIBUTE)
            container.setBoolean(REDSTONE_ATTRIBUTE, !redstone) // Just flip the boolean value
            doubleChest?.persistentDataContainer?.setBoolean(REDSTONE_ATTRIBUTE, !redstone)
            return LockReturnValue(true, if (redstone) "Redstone protection removed." else "Redstone protection added.")
        }
        return LockReturnValue(false, "No permission.")
    }

    fun addFriend(player: String, newFriend: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return LockReturnValue(false, "No permission.")
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        // This is a new friend to add. Don't add them if they've already got access
        if (!access.contains(newFriend)) {
            access = access.plus(newFriend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.setString(LOCK_ATTRIBUTE, access.toString())
            return LockReturnValue(true, "Permission granted.")
        }
        return LockReturnValue(false, "Friend already added.")
    }

    fun removeFriend(player: String, friend: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return LockReturnValue(false, "No permission.")
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        if (access.contains(friend)) {
            access = access.minus(friend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.setString(LOCK_ATTRIBUTE, access.toString())
            return LockReturnValue(true, "Permission removed.")
        }
        return LockReturnValue(false, "Friend does not exist")
    }

    data class LockReturnValue(val success: Boolean, val message: String)
}
