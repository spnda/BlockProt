package de.sean.blockprot.bukkit.nbt

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.util.LockUtil
import de.sean.blockprot.bukkit.util.LockUtil.parseStringList
import de.tr7zw.changeme.nbtapi.NBTBlock
import de.tr7zw.changeme.nbtapi.NBTCompound
import de.tr7zw.changeme.nbtapi.NBTTileEntity
import org.bukkit.block.Block
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.entity.Player
import java.util.*

class BlockLockHandler constructor(val block: Block) {
    private var container: NBTCompound = when {
        LockUtil.isLockableBlock(block.type) -> NBTBlock(block).data
        LockUtil.isLockableTileEntity(block.type) -> NBTTileEntity(block.state).persistentDataContainer
        else -> throw RuntimeException("Given block ${block.type} is not a lockable block/tile entity")
    }

    /**
     * Reads the current owner from the NBT container.
     * @return The owner as a UUID-String read from the container, or an empty String.
     */
    fun getOwner(): String = container.getString(OWNER_ATTRIBUTE) ?: ""

    /**
     * Gets the list of friends that are allowed to access the container.
     * @return A list of UUID-Strings which each represent a player's UUID.
     */
    fun getAccess() = parseStringList(container.getString(LOCK_ATTRIBUTE))

    /**
     * Read the access flags of this block.
     */
    fun getBlockAccessFlags(): EnumSet<BlockAccessFlag> =
        if (!container.hasKey(ACCESS_FLAGS_ATTRIBUTE)) EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE)
        else BlockAccessFlag.parseFlags(container.getInteger(ACCESS_FLAGS_ATTRIBUTE))

    /**
     * If true, redstone should be allowed for this block and should not be blocked.
     * If redstone has not been set for this block yet, the default value is true
     * @return Whether redstone should be allowed or not.
     */
    fun getRedstone(): Boolean =
        if (!container.hasKey(REDSTONE_ATTRIBUTE)) true // Default value
        else container.getBoolean(REDSTONE_ATTRIBUTE)

    /**
     * Set the current owner of this block.
     */
    fun setOwner(string: String) = container.setString(OWNER_ATTRIBUTE, string)

    /**
     * Set the current list of friends that have access to this block.
     */
    fun setAccess(list: List<String>) = container.setString(LOCK_ATTRIBUTE, list.toString())

    /**
     * Sets the access flags for this block. ORs all flags together to one integer, then
     * writes all of them to ACCESS_FLAGS_ATTRIBUTE.
     */
    fun setBlockAccessFlags(flags: EnumSet<BlockAccessFlag>) = container.setInteger(
        ACCESS_FLAGS_ATTRIBUTE,
        flags.map { it.flag }.fold(0) { acc, i -> acc.or(i) }
    )

    /**
     * If true, redstone should be allowed for this block and should not be blocked
     */
    fun setRedstone(redstone: Boolean) = container.setBoolean(REDSTONE_ATTRIBUTE, redstone)

    fun isNotProtected() = getOwner().isEmpty() && getAccess().isEmpty()
    fun isProtected() = !isNotProtected()

    fun isOwner(player: String) = getOwner() == player

    /**
     * Checks whether or not given [player] can access this block.
     */
    fun canAccess(player: String) =
        if (isProtected()) (getOwner() == player || getAccess().contains(player)) else getOwner().isEmpty()

    fun lockBlock(player: Player, isOp: Boolean, doubleChest: NBTTileEntity?): LockReturnValue {
        var owner = getOwner()
        val playerUuid = player.uniqueId.toString()
        if (owner.isEmpty()) {
            // This block is not owned by anyone, this user can claim this block
            owner = playerUuid
            container.setString(OWNER_ATTRIBUTE, owner)
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, owner)
            return LockReturnValue(true, Translator.get(TranslationKey.MESSAGES__PERMISSION_GRANTED))
        } else if (isOwner(playerUuid) ||
            (isOp && owner.isNotEmpty()) ||
            player.hasPermission(PERMISSION_ADMIN)
        ) {
            setOwner(""); setAccess(emptyList())
            doubleChest?.persistentDataContainer?.setString(OWNER_ATTRIBUTE, "")
            doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, "") // Also clear the friends
            return LockReturnValue(true, Translator.get(TranslationKey.MESSAGES__UNLOCKED))
        }
        return LockReturnValue(false, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
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
                redstone = !container.getBoolean(REDSTONE_ATTRIBUTE)
                container.setBoolean(REDSTONE_ATTRIBUTE, redstone) // Just flip the boolean value
            }
            doubleChest?.persistentDataContainer?.setBoolean(REDSTONE_ATTRIBUTE, !redstone)
            return LockReturnValue(
                true,
                if (redstone) Translator.get(TranslationKey.MESSAGES__REDSTONE_REMOVED)
                else Translator.get(TranslationKey.MESSAGES__REDSTONE_ADDED)
            )
        }
        return LockReturnValue(false, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
    }

    fun modifyFriends(player: String, friend: String, modifyAction: FriendModifyAction, doubleChest: NBTTileEntity?): LockReturnValue {
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (container.getString(OWNER_ATTRIBUTE) != player) return LockReturnValue(
            false,
            Translator.get(TranslationKey.MESSAGES__NO_PERMISSION)
        )
        var access = parseStringList(container.getString(LOCK_ATTRIBUTE))
        when (modifyAction) {
            FriendModifyAction.ADD_FRIEND -> {
                if (!access.contains(friend)) {
                    access = access.plus(friend)
                    container.setString(LOCK_ATTRIBUTE, access.toString())
                    doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, access.toString())
                    return LockReturnValue(true, Translator.get(TranslationKey.MESSAGES__FRIEND_ADDED))
                } else {
                    return LockReturnValue(false, Translator.get(TranslationKey.MESSAGES__FRIEND_ALREADY_ADDED))
                }
            }
            FriendModifyAction.REMOVE_FRIEND -> {
                if (access.contains(friend)) {
                    access = access.minus(friend)
                    container.setString(LOCK_ATTRIBUTE, access.toString())
                    doubleChest?.persistentDataContainer?.setString(LOCK_ATTRIBUTE, access.toString())
                    return LockReturnValue(true, Translator.get(TranslationKey.MESSAGES__FRIEND_REMOVED))
                } else {
                    return LockReturnValue(false, Translator.get(TranslationKey.MESSAGES__FRIEND_CANT_BE_REMOVED))
                }
            }
        }
    }

    /**
     * Copy the data over from the top or bottom door to the other half
     */
    fun applyToDoor(block: Block) {
        if (LockUtil.isLockableDoor(block.type)) {
            val blockState = block.state
            val door = blockState.blockData as Door
            var other = blockState.location
            other = if (door.half == Bisected.Half.TOP) other.subtract(0.0, 1.0, 0.0)
            else other.add(0.0, 1.0, 0.0)
            val otherDoor = blockState.world.getBlockAt(other)
            val otherDoorHandler = BlockLockHandler(otherDoor)
            otherDoorHandler.setOwner(this.getOwner())
            otherDoorHandler.setAccess(this.getAccess())
            otherDoorHandler.setRedstone(this.getRedstone())
        }
    }

    companion object {
        const val OWNER_ATTRIBUTE = "splugin_owner"
        const val LOCK_ATTRIBUTE = "splugin_lock"
        const val REDSTONE_ATTRIBUTE = "splugin_lock_redstone"
        const val LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place"
        const val DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends"
        const val ACCESS_FLAGS_ATTRIBUTE = "blockprot_access_flags"

        const val PERMISSION_LOCK = "blockprot.lock"
        const val PERMISSION_INFO = "blockprot.info"
        const val PERMISSION_ADMIN = "blockprot.admin"
        const val PERMISSION_BYPASS = "blockprot.bypass"
    }
}
