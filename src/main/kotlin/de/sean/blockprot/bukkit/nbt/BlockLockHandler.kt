@file:Suppress("LiftReturnOrAssignment")

package de.sean.blockprot.bukkit.nbt

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.bukkit.nbt.LockUtil.LOCK_ATTRIBUTE
import de.sean.blockprot.bukkit.nbt.LockUtil.OWNER_ATTRIBUTE
import de.sean.blockprot.bukkit.nbt.LockUtil.REDSTONE_ATTRIBUTE
import de.sean.blockprot.bukkit.nbt.LockUtil.parseStringList
import de.tr7zw.nbtapi.NBTBlock
import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.block.Block
import org.bukkit.entity.Player

class BlockLockHandler constructor(val block: Block) {
    private var container: NBTCompound

    init {
        when (block.type) {
            in LockUtil.lockableBlocks -> container = NBTBlock(block).data
            in LockUtil.lockableTileEntities -> container = NBTTileEntity(block.state).persistentDataContainer
            else -> throw RuntimeException("Given block ${block.type} is not a lockable block/tile entity")
        }
    }

    fun getOwner(): String = container.getString(OWNER_ATTRIBUTE) ?: ""
    fun getAccess() = parseStringList(container.getString(LOCK_ATTRIBUTE))

    /**
     * If true, redstone should be allowed for this block and should not be blocked.
     * The default value is true
     */
    fun getRedstone(): Boolean {
        return if (!container.hasKey(REDSTONE_ATTRIBUTE)) true // Default value
        else container.getBoolean(REDSTONE_ATTRIBUTE)
    }

    fun setOwner(string: String) = container.setString(OWNER_ATTRIBUTE, string)
    fun setAccess(list: List<String>) = container.setString(LOCK_ATTRIBUTE, list.toString())

    /**
     * If true, redstone should be allowed for this block and should not be blocked
     */
    fun setRedstone(redstone: Boolean) = container.setBoolean(REDSTONE_ATTRIBUTE, redstone)

    fun isNotProtected() = getOwner().isEmpty() && getAccess().isEmpty()
    fun isProtected() = !isNotProtected()

    fun isOwner(player: String) = getOwner() == player
    fun canAccess(player: String) =
        if (isProtected()) (getOwner() == player || getAccess().contains(player)) else getOwner().isEmpty()

    fun lockBlock(player: Player, isOp: Boolean, doubleChest: NBTTileEntity?): LockReturnValue {
        var owner = container.getString(OWNER_ATTRIBUTE) ?: ""
        val playerUuid = player.uniqueId.toString()
        if (owner.isEmpty()) {
            // This block is not owned by anyone, this user can claim this block
            owner = playerUuid
            container.setString(OWNER_ATTRIBUTE, owner)
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, owner)
            return LockReturnValue(true, BlockProt.translator.get(TranslationKey.MESSAGES__PERMISSION_GRANTED))
        } else if ((owner == playerUuid) ||
            (isOp && owner.isNotEmpty()) ||
            player.hasPermission(LockUtil.PERMISSION_ADMIN)
        ) {
            container.setString(OWNER_ATTRIBUTE, "")
            container.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, "")
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            return LockReturnValue(true, BlockProt.translator.get(TranslationKey.MESSAGES__UNLOCKED))
        }
        return LockReturnValue(false, BlockProt.translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
    }

    fun lockRedstoneForBlock(player: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        if (owner == player) {
            val redstone: Boolean
            if (!container.hasKey(REDSTONE_ATTRIBUTE)) {
                /* We assume that our current value is true, and we'll therefore change it off */
                redstone = false
                container.setBoolean(REDSTONE_ATTRIBUTE, redstone)
            } else {
                redstone = container.getBoolean(REDSTONE_ATTRIBUTE)
                container.setBoolean(REDSTONE_ATTRIBUTE, !redstone) // Just flip the boolean value
            }
            doubleChest?.persistentDataContainer?.setBoolean(REDSTONE_ATTRIBUTE, !redstone)
            return LockReturnValue(
                true,
                if (redstone) BlockProt.translator.get(TranslationKey.MESSAGES__REDSTONE_ADDED)
                else BlockProt.translator.get(TranslationKey.MESSAGES__REDSTONE_REMOVED)
            )
        }
        return LockReturnValue(false, BlockProt.translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
    }

    fun addFriend(player: String, newFriend: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return LockReturnValue(
            false,
            BlockProt.translator.get(TranslationKey.MESSAGES__NO_PERMISSION)
        )
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        // This is a new friend to add. Don't add them if they've already got access
        if (!access.contains(newFriend)) {
            access = access.plus(newFriend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, access.toString())
            return LockReturnValue(true, BlockProt.translator.get(TranslationKey.MESSAGES__FRIEND_ADDED))
        }
        return LockReturnValue(false, BlockProt.translator.get(TranslationKey.MESSAGES__FRIEND_ALREADY_ADDED))
    }

    fun removeFriend(player: String, friend: String, doubleChest: NBTTileEntity?): LockReturnValue {
        val owner = container.getString(OWNER_ATTRIBUTE)
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (owner != player) return LockReturnValue(
            false,
            BlockProt.translator.get(TranslationKey.MESSAGES__NO_PERMISSION)
        )
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        if (access.contains(friend)) {
            access = access.minus(friend)
            container.setString(LOCK_ATTRIBUTE, access.toString())
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, access.toString())
            return LockReturnValue(true, BlockProt.translator.get(TranslationKey.MESSAGES__FRIEND_REMOVED))
        }
        return LockReturnValue(false, BlockProt.translator.get(TranslationKey.MESSAGES__FRIEND_CANT_BE_REMOVED))
    }

    data class LockReturnValue(val success: Boolean, val message: String)
}
