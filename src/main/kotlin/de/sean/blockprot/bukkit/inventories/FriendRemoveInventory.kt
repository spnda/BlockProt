package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

object FriendRemoveInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName: String = Strings.getString("inventories.remove_friend.name", "Remove Friend")

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
                            handler.removeFriend(
                                player.uniqueId.toString(),
                                friend,
                                if (doubleChest != null) NBTTileEntity(doubleChest) else null
                            )
                        }
                    }
                    InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                        modifyFriends(player) {
                            it.remove(friend)
                        }
                        player.closeInventory()
                    }
                }
            }
            else -> {}
        }
        event.isCancelled = true
    }

    fun createInventoryAndFill(friendsToRemove: List<String>): Inventory {
        val inv = createInventory()
        // Get the skulls asynchronously and add them one after each other.
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            val skulls: MutableList<ItemStack> = ArrayList()
            while (i < 9 * 3 - 2 && i < friendsToRemove.size) {
                val skull = ItemUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(friendsToRemove[i])))
                skulls.add(skull)
                i++
            }
            Bukkit.getScheduler().runTask(BlockProt.instance) { _ ->
                for (skull in skulls.indices)
                    inv.setItem(skull, skulls[skull])
            }
        }
        inv.setItem(9 * 3 - 1, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
        return inv
    }
}
