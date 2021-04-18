package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.SkullMeta

object FriendAddInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName = Strings.getString("inventories.add_friend.name", "Add Friend")

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                if (state == null) return
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
            Material.PLAYER_HEAD -> {
                if (state == null) return
                val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                val friend = skull.owningPlayer?.uniqueId.toString()
                when (state.friendSearchState) {
                    InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                        if (state.block == null) return
                        val handler = BlockLockHandler(state.block)
                        val doubleChest = getDoubleChest(state.block, player.world)
                        applyChangesAndExit(handler, player) {
                            handler.addFriend(
                                player.uniqueId.toString(),
                                friend,
                                if (doubleChest != null) NBTTileEntity(doubleChest) else null
                            )
                        }
                    }
                    InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                        modifyFriends(player) {
                            it.add(friend)
                        }
                        player.closeInventory()
                    }
                }
            }
            Material.MAP -> {
                FriendSearchInventory.openAnvilInventory(player)
            }
            else -> {}
        }
        event.isCancelled = true
    }

    fun filterFriendsList(current: List<String>, allPlayers: List<Player>, self: String): MutableList<Player> {
        val ret: MutableList<Player> = ArrayList()
        for (player in allPlayers) {
            val playerUuid = player.uniqueId.toString()
            if (!current.contains(playerUuid) && playerUuid != self) ret.add(player)
        }
        return ret
    }

    fun createInventoryAndFill(friendsToAdd: MutableList<Player>): Inventory {
        val inv = createInventory()
        var i = 0
        while (i < 9 * 3 - 3 && i < friendsToAdd.size) {
            inv.setItem(i, ItemUtil.getPlayerSkull(friendsToAdd[i]))
            i++
        }
        inv.setItem(9 * 3 - 2, ItemUtil.getItemStack(1, Material.MAP, Strings.SEARCH))
        inv.setItem(9 * 3 - 1, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
        return inv
    }
}
