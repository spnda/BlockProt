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

import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import java.util.*

/**
 * Storage for the current state and location of each player's
 * interactions with this plugin's menus.
 */
data class InventoryState(val block: Block?) {
    enum class FriendSearchState {
        FRIEND_SEARCH,
        DEFAULT_FRIEND_SEARCH,
    }

    /**
     * The current state of the friend search mechanism.
     * When adding default friends this should be [FriendSearchState.DEFAULT_FRIEND_SEARCH],
     * which will then add the new friends to the current players NBT.
     * When adding friends for a single block this should be [FriendSearchState.FRIEND_SEARCH],
     * which will then add the new friends to [block]'s NBT.
     */
    var friendSearchState: FriendSearchState = FriendSearchState.FRIEND_SEARCH

    var friendResultCache: MutableList<OfflinePlayer> = mutableListOf()

    /**
     * The current index of the [BlockProtInventory] page.
     */
    var friendPage: Int = 0

    /**
     * The friend we currently want to modify with [FriendDetailInventory].
     */
    var curFriend: OfflinePlayer? = null

    companion object {
        /**
         * HashMap containing the current InventoryState of each player.
         * The keys are the String representation of the player's UUID.
         */
        private val players = HashMap<String, InventoryState>()

        fun set(player: String, state: InventoryState) {
            players[player] = state
        }

        fun set(player: UUID, state: InventoryState) {
            players[player.toString()] = state
        }

        fun get(player: String) = players[player]

        fun get(player: UUID) = players[player.toString()]

        fun remove(player: String) {
            players.remove(player)
        }

        fun remove(player: UUID) {
            players.remove(player.toString())
        }
    }
}
