package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.util.ItemUtil
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.util.*

object FriendRemoveInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName: String = Translator.get(TranslationKey.INVENTORIES__FRIENDS__REMOVE)

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
                val index = findItemIndex(event.inventory, item)
                val friend = state.friendResultCache[index]
                when (state.friendSearchState) {
                    InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                        if (state.block == null) return
                        val doubleChest = getDoubleChest(state.block, player.world)
                        applyChanges(state.block, player, exit = true) {
                            it.modifyFriends(
                                player.uniqueId.toString(),
                                friend.uniqueId.toString(),
                                BlockLockHandler.FriendModifyAction.REMOVE_FRIEND,
                                if (doubleChest != null) NBTTileEntity(doubleChest) else null
                            )
                        }
                    }
                    InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                        modifyFriends(player) {
                            it.remove(friend.uniqueId.toString())
                        }
                        player.closeInventory()
                    }
                }
            }
            else -> {
            }
        }
        event.isCancelled = true
    }

    fun createInventoryAndFill(player: Player, friendsToRemove: List<String>): Inventory {
        val inv = createInventory()
        val state = InventoryState.get(player.uniqueId) ?: return inv

        state.friendResultCache.clear()
        for (i in friendsToRemove.indices) {
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(friendsToRemove[i]))
            inv.setItem(i, ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name))
            state.friendResultCache.add(offlinePlayer)
        }
        inv.setItem(
            9 * 3 - 1,
            ItemUtil.getItemStack(
                1,
                Material.BLACK_STAINED_GLASS_PANE,
                Translator.get(TranslationKey.INVENTORIES__BACK)
            )
        )

        // Get the skulls asynchronously and add them one after each other.
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            while (i < 9 * 3 - 2 && i < state.friendResultCache.size) {
                val skull = ItemUtil.getPlayerSkull(state.friendResultCache[i])
                inv.setItem(i, skull)
                i++
            }
        }

        return inv
    }
}
