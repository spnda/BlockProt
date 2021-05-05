package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.bukkit.nbt.LockUtil.parseStringList
import de.sean.blockprot.util.ItemUtil
import de.tr7zw.nbtapi.NBTEntity
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

object FriendSearchResultInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName = Translator.get(TranslationKey.INVENTORIES__FRIENDS__RESULT)

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                // Go back to the search if nothing was found
                FriendSearchInventory.openAnvilInventory(player)
            }
            Material.PLAYER_HEAD -> {
                if (state == null) return
                val index = findItemIndex(event.inventory, item)
                val friend = state.friendResultCache[index]
                when (state.friendSearchState) {
                    InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                        if (state.block == null) return
                        val handler = BlockLockHandler(state.block)
                        val doubleChest = getDoubleChest(state.block, player.world)
                        applyChangesAndExit(handler, player) {
                            handler.modifyFriends(
                                player.uniqueId.toString(),
                                friend.uniqueId.toString(),
                                BlockLockHandler.FriendModifyAction.ADD_FRIEND,
                                if (doubleChest != null) NBTTileEntity(doubleChest) else null
                            )
                        }
                    }
                    InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                        modifyFriends(player) {
                            it.add(friend.uniqueId.toString())
                        }
                        player.closeInventory()
                    }
                }
            }
            Material.BARRIER -> {
                player.closeInventory()
            }
            else -> {
            }
        }
        event.isCancelled = true
    }

    /**
     * Create an inventory for given [player]. [players] should be a list of all players,
     * excluding [player].
     */
    fun createInventoryAndFill(player: Player, players: List<OfflinePlayer>): Inventory? {
        val state = InventoryState.get(player.uniqueId) ?: return null

        // The already existing friends we want to add to.
        val friends: List<String> = when (state.friendSearchState) {
            InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                val block = state.block ?: return null
                val handler = BlockLockHandler(block)
                handler.getAccess()
            }
            InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                val playerNBT = NBTEntity(player).persistentDataContainer
                parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
            }
        }

        // We'll filter all doubled friends out of the list and add them to the current InventoryState.
        val foundPlayers = players.filter {
            when {
                friends.contains(it.uniqueId.toString()) -> false
                else -> true
            }
        }
        state.friendResultCache.clear()
        state.friendResultCache.addAll(foundPlayers)

        // To not delay when the inventory opens, we'll asynchronously get the items after
        // the inventory has been opened and later add them to the inventory. In the meantime,
        // we'll show the same amount of skeleton heads.
        val inv = createInventory()
        val maxPlayers = foundPlayers.size.coerceAtMost(9 * 3 - 2)
        for (i in 0 until maxPlayers) {
            inv.setItem(i, ItemUtil.getItemStack(1, Material.SKELETON_SKULL, foundPlayers[i].name))
        }
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            // Only show the 9 * 3 - 2 most relevant players. Don't show any more.
            var playersIndex = 0
            while (playersIndex < maxPlayers && playersIndex < foundPlayers.size) {
                // Only add to the inventory if this is not a friend (yet)
                val newFriend = foundPlayers[playersIndex]
                inv.setItem(playersIndex, ItemUtil.getPlayerSkull(newFriend))
                playersIndex += 1
            }
        }
        inv.setItem(9 * 3 - 1, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, null, null))
        return inv
    }
}
