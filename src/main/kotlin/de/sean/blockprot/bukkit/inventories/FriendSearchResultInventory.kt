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
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Material
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
                // As in the anvil inventory we cannot differentiate between
                // pressing Escape to go back, or closing it to go to the result
                // inventory, we won't return to the anvil inventory and instead
                // go right back to the FriendAddInventory.
                if (state == null) {
                    player.closeInventory()
                    return
                }
                val currentFriends: List<String> = when (state.friendSearchState) {
                    InventoryState.FriendSearchState.FRIEND_SEARCH -> when (state.block) {
                        null -> emptyList()
                        else -> BlockLockHandler(state.block).getAccess()
                    }
                    InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                        val nbtEntity = NBTEntity(player).persistentDataContainer
                        parseStringList(nbtEntity.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
                    }
                }
                val friendsToAdd = FriendAddInventory.filterFriendsList(
                    currentFriends,
                    Bukkit.getOnlinePlayers().toList(),
                    player.uniqueId.toString()
                )
                val inv = FriendAddInventory.createInventoryAndFill(friendsToAdd)
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
     * Compare two strings by the levenshtein distance, returning a value between 0,
     * being totally unrelated strings, and 1, being identical or if both are empty.
     */
    private fun compareStrings(s1: String, s2: String): Double {
        var longer = s1
        var shorter = s2
        if (s1.length < s2.length) {
            longer = s2; shorter = s1
        }
        val longerLength = longer.length
        return if (longerLength == 0) 1.0 // They match 100% if both Strings are empty
        else (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) / longerLength.toDouble()
    }

    /**
     * Create an inventory for given [player].
     */
    fun createInventoryAndFill(player: Player, searchQuery: String): Inventory? {
        var players = Bukkit.getOfflinePlayers().toList()
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
        players = players.filter {
            // Filter all the players by search criteria.
            // If the strings are similar by 30%, the strings are considered similar (imo) and should be added.
            // If they're less than 30% similar, we should still check if it possibly contains the search criteria
            // and still add that user.
            when {
                it.name == null || it.uniqueId == player.uniqueId -> false
                friends.contains(it.uniqueId.toString()) -> false
                compareStrings(it.name!!, searchQuery) > 0.3 -> true
                else -> it.name!!.contains(searchQuery, ignoreCase = true)
            }
        }
        state.friendResultCache.clear()
        state.friendResultCache.addAll(players)

        // To not delay when the inventory opens, we'll asynchronously get the items after
        // the inventory has been opened and later add them to the inventory. In the meantime,
        // we'll show the same amount of skeleton heads.
        val inv = createInventory()
        val maxPlayers = players.size.coerceAtMost(9 * 3 - 2)
        for (i in 0 until maxPlayers) {
            inv.setItem(i, ItemUtil.getItemStack(1, Material.SKELETON_SKULL, players[i].name))
        }
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            // Only show the 9 * 3 - 2 most relevant players. Don't show any more.
            var playersIndex = 0
            while (playersIndex < maxPlayers && playersIndex < players.size) {
                // Only add to the inventory if this is not a friend (yet)
                inv.setItem(playersIndex, ItemUtil.getPlayerSkull(players[playersIndex]))
                playersIndex += 1
            }
        }
        inv.setItem(
            9 * 3 - 1,
            ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Translator.get(TranslationKey.INVENTORIES__BACK), null)
        )
        return inv
    }
}
