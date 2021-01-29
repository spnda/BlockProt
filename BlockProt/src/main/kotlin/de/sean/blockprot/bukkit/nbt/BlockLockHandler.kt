package de.sean.blockprot.bukkit.nbt

import de.sean.blockprot.bukkit.nbt.LockUtil.LOCK_ATTRIBUTE
import de.sean.blockprot.bukkit.nbt.LockUtil.OWNER_ATTRIBUTE
import de.sean.blockprot.bukkit.nbt.LockUtil.REDSTONE_ATTRIBUTE
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTBlock
import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.block.Block
import java.util.ArrayList

class BlockLockHandler constructor(block: Block) {
    private var container: NBTCompound

    init {
        when (block.type) {
            in LockUtil.lockableBlocks -> container = NBTBlock(block).data
            in LockUtil.lockableTileEntities -> container = NBTTileEntity(block.state).persistentDataContainer
            else -> throw RuntimeException("Given block is not a lockable block/tile entity")
        }
    }

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
            return LockReturnValue(true, Strings.PERMISSION_GRANTED)
        } else if ((owner == player) || (isOp && owner.isNotEmpty())) {
            container.setString(OWNER_ATTRIBUTE, "")
            container.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, "")
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            return LockReturnValue(true, Strings.UNLOCKED)
        }
        return LockReturnValue(false, Strings.NO_PERMISSION)
    }

    fun lockRedstoneForBlock(player: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        if (owner == player) {
            val redstone = container.getBoolean(REDSTONE_ATTRIBUTE)
            container.setBoolean(REDSTONE_ATTRIBUTE, !redstone) // Just flip the boolean value
            doubleChest?.persistentDataContainer?.setBoolean(REDSTONE_ATTRIBUTE, !redstone)
            return if (redstone)
                LockReturnValue(true, Strings.REDSTONE_ADDED)
            else
                LockReturnValue(true, Strings.REDSTONE_REMOVED)
        }
        return LockReturnValue(false, Strings.NO_PERMISSION)
    }

    fun addFriend(player: String, newFriend: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return LockReturnValue(false, Strings.NO_PERMISSION)
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        // This is a new friend to add. Don't add them if they've already got access
        if (!access.contains(newFriend)) {
            access = access.plus(newFriend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, access.toString())
            return LockReturnValue(true, Strings.FRIEND_ADDED)
        }
        return LockReturnValue(false, Strings.FRIEND_ALREADY_ADDED)
    }

    fun removeFriend(player: String, friend: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return LockReturnValue(false, Strings.NO_PERMISSION)
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        if (access.contains(friend)) {
            access = access.minus(friend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, access.toString())
            return LockReturnValue(true, Strings.FRIEND_REMOVED)
        }
        return LockReturnValue(false, Strings.FRIEND_CANT_BE_REMOVED)
    }

    data class LockReturnValue(val success: Boolean, val message: String)
}
