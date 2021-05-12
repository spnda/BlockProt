package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.FriendModifyAction
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

interface BlockFriendModifyInventory : BlockProtInventory {
    fun modifyFriendsForAction(state: InventoryState, player: Player, friend: OfflinePlayer, action: FriendModifyAction, exit: Boolean) {
        when (state.friendSearchState) {
            InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                if (state.block != null) {
                    val doubleChest = LockUtil.getDoubleChest(state.block, player.world)
                    applyChanges(state.block, player, exit = exit) {
                        it.modifyFriends(
                            player.uniqueId.toString(),
                            friend.uniqueId.toString(),
                            action,
                            if (doubleChest != null) NBTTileEntity(doubleChest) else null
                        )
                    }
                }
            }
            InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                modifyFriends(player, exit = exit) {
                    when (action) {
                        FriendModifyAction.ADD_FRIEND -> it.add(friend.uniqueId.toString())
                        FriendModifyAction.REMOVE_FRIEND -> it.remove(friend.uniqueId.toString())
                    }
                }
            }
        }
    }

    fun exitModifyInventory(player: Player, state: InventoryState) {
        player.closeInventory()
        val inv = when (state.friendSearchState) {
            InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                if (state.block == null) return
                BlockLockInventory.createInventoryAndFill(
                    player,
                    state.block.state.type,
                    BlockLockHandler(state.block)
                )
            }
            InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                UserSettingsInventory.createInventoryAndFill(player)
            }
        }
        player.openInventory(inv)
    }
}
