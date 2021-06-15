/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
     * The current index of the [FriendModifyInventory] page.
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
