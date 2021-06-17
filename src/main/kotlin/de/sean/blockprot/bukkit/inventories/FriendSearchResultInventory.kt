/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.nbt.FriendModifyAction
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler
import de.sean.blockprot.bukkit.util.ItemUtil
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class FriendSearchResultInventory : FriendModifyInventory() {
    override fun getSize() = InventoryConstants.tripleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__FRIENDS__RESULT)

    override fun onClick(event: InventoryClickEvent, state: InventoryState) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                // As in the anvil inventory we cannot differentiate between
                // pressing Escape to go back, or closing it to go to the result
                // inventory, we won't return to the anvil inventory and instead
                // go right back to the FriendAddInventory.
                player.openInventory(FriendManageInventory().fill(player))
            }
            Material.PLAYER_HEAD, Material.SKELETON_SKULL -> {
                val index = findItemIndex(item)
                val friend = state.friendResultCache[index]
                modifyFriendsForAction(state, player, friend, FriendModifyAction.ADD_FRIEND, false)
                player.openInventory(FriendManageInventory().fill(player))
            }
            else -> exit(player)
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState) {}

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
                val handler = BlockNBTHandler(block)
                handler.friends.map { it.name }
            }
            InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                val settingsHandler = PlayerSettingsHandler(player)
                settingsHandler.defaultFriends
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
        setBackButton()
        return inventory
    }
}
