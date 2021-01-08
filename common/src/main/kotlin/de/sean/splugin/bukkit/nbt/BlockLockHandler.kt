package de.sean.splugin.bukkit.nbt

import de.tr7zw.nbtapi.NBTTileEntity
import java.util.ArrayList

class BlockLockHandler(nbtTileEntity: NBTTileEntity) {
    companion object {
        const val OWNER_ATTRIBUTE = "splugin_owner"
        const val LOCK_ATTRIBUTE = "splugin_lock"
        const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"
    }

    private val entity = nbtTileEntity
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
    fun canAccess(player: String) = getOwner() == player || getAccess().contains(player)

    fun lockBlock(player: String, isOp: Boolean, doubleChest: NBTTileEntity?): Pair<Boolean, String> {
        var owner = container.getString(OWNER_ATTRIBUTE)
        if ((isOp && (owner == null || owner == "")) || (owner == null || owner == "")) {
            // This block is not owned by anyone, this user can claim this block
            owner = player
            container.setString(OWNER_ATTRIBUTE, owner)
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, null)
            return Pair(true, "Permission granted.")
        } else if ((owner == player) || (isOp && owner != "")) {
            container.setString(OWNER_ATTRIBUTE, null)
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, null)
            return Pair(true, "Unlocked.")
        }
        return Pair(false, "No permission.")
    }

    fun lockRedstoneForBlock(player: String, doubleChest: NBTTileEntity?): Pair<Boolean, String> {
        val owner = container.getString(OWNER_ATTRIBUTE)
        if (owner == player) {
            val redstone = container.getBoolean(REDSTONE_ATTRIBUTE)
            container.setBoolean(REDSTONE_ATTRIBUTE, !redstone) // Just flip the boolean value
            doubleChest?.persistentDataContainer?.setBoolean(REDSTONE_ATTRIBUTE, !redstone)
            return Pair(true, if (redstone) "Redstone protection removed." else "Redstone protection added.")
        }
        return Pair(false, "No permission.")
    }

    fun addFriend(player: String, newFriend: String, doubleChest: NBTTileEntity?): Pair<Boolean, String> {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return Pair(false, "No permission.")
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        // This is a new friend to add. Don't add them if they've already got access
        if (!access.contains(newFriend)) {
            access = access.plus(newFriend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.setString(LOCK_ATTRIBUTE, access.toString())
            return Pair(true, "Permission granted.")
        }
        return Pair(false, "Friend already added.")
    }

    fun removeFriend(player: String, friend: String, doubleChest: NBTTileEntity?): Pair<Boolean, String> {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return Pair(false, "No permission.")
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        if (access.contains(friend)) {
            access = access.minus(friend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.setString(LOCK_ATTRIBUTE, access.toString())
            return Pair(true, "Permission removed.")
        }
        return Pair(false, "Friend does not exist")
    }
}
