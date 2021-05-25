package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.FriendModifyAction
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.parseStringList
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.setBackButton
import de.tr7zw.changeme.nbtapi.NBTEntity
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

class FriendSearchResultInventory : FriendModifyInventory() {
    override fun getSize() = InventoryConstants.tripleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__FRIENDS__RESULT)

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
                player.openInventory(FriendManageInventory().fill(player))
            }
            Material.PLAYER_HEAD, Material.SKELETON_SKULL -> {
                if (state == null) return
                val index = findItemIndex(item)
                val friend = state.friendResultCache[index]
                modifyFriendsForAction(state, player, friend, FriendModifyAction.ADD_FRIEND, false)
                player.openInventory(FriendManageInventory().fill(player))
            }
            else -> exit(player)
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
    fun fill(player: Player, searchQuery: String): Inventory? {
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
        val maxPlayers = players.size.coerceAtMost(InventoryConstants.tripleLine - 2)
        for (i in 0 until maxPlayers) {
            inventory.setItem(i, ItemUtil.getItemStack(1, Material.SKELETON_SKULL, players[i].name))
        }
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            // Only show the 9 * 3 - 2 most relevant players. Don't show any more.
            var playersIndex = 0
            while (playersIndex < maxPlayers && playersIndex < players.size) {
                // Only add to the inventory if this is not a friend (yet)
                inventory.setItem(playersIndex, ItemUtil.getPlayerSkull(players[playersIndex]))
                playersIndex += 1
            }
        }
        inventory.setBackButton()
        return inventory
    }
}
