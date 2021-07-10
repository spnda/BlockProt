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

import de.sean.blockprot.bukkit.events.BlockAccessEditMenuEvent
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import java.util.*

/**
 * Storage for the current state and location of each player's
 * interactions with this plugin's menus.
 * @since 0.1.9
 */
data class InventoryState(val block: Block?) {
    /**
     * The current search state of the friend menu. Indicates
     * whether we're searching for default friends or for friends
     * to be added directly to a block.
     * @since 0.1.9
     */
    enum class FriendSearchState {
        /**
         * This search is currently for a single block.
         * @since 0.1.9
         */
        FRIEND_SEARCH,

        /**
         * This search is currently for the default friends
         * of a player.
         * @since 0.1.9
         */
        DEFAULT_FRIEND_SEARCH,
    }

    /**
     * The current state of the friend search mechanism.
     * When adding default friends this should be [FriendSearchState.DEFAULT_FRIEND_SEARCH],
     * which will then add the new friends to the current players NBT.
     * When adding friends for a single block this should be [FriendSearchState.FRIEND_SEARCH],
     * which will then add the new friends to [block]'s NBT.
     * @since 0.1.9
     */
    var friendSearchState: FriendSearchState = FriendSearchState.FRIEND_SEARCH

    /**
     * A local cache of offline players for this state.
     * @since 0.1.13
     */
    var friendResultCache: MutableList<OfflinePlayer> = mutableListOf()

    /**
     * The current index of the [BlockProtInventory] page.
     * @since 0.2.2
     */
    var friendPage: Int = 0

    /**
     * The friend we currently want to modify with [FriendDetailInventory].
     * @since 0.2.2
     */
    var curFriend: OfflinePlayer? = null

    /**
     * The current cached menu access for this state.
     * @since 0.4.0
     */
    var menuAccess: BlockAccessEditMenuEvent.MenuAccess = BlockAccessEditMenuEvent.MenuAccess.NONE

    /**
     * @since 0.1.9
     */
    companion object {
        /**
         * HashMap containing the current InventoryState of each player.
         * The keys are the String representation of the player's UUID.
         * @since 0.1.9
         */
        private val players = HashMap<String, InventoryState>()

        /**
         * Set's [state] to the UUID compatible String [player]. Overrides any previous state.
         * @since 0.1.9
         */
        fun set(player: String, state: InventoryState) {
            players[player] = state
        }

        /**
         * Set's [state] to the player with UUID [player].Overrides
         * any previous state.
         * @since 0.1.9
         */
        fun set(player: UUID, state: InventoryState) {
            players[player.toString()] = state
        }

        /**
         * Get the state for the UUID [player]. Might
         * be null, if [player] is not a valid UUID or
         * there is no state for that player currently.
         * @since 0.1.9
         */
        fun get(player: String) = players[player]

        /**
         * Get the state for the UUID [player]. Might
         * be null, if [player] is not a valid UUID or
         * there is no state for that player currently.
         * @since 0.1.9
         */
        fun get(player: UUID) = players[player.toString()]

        /**
         * Removes the state for [player]. This will not throw
         * an exception if there was no state for [player].
         * @since 0.1.9
         */
        fun remove(player: String) {
            players.remove(player)
        }

        /**
         * Removes the state for [player]. This will not throw
         * an exception if there was no state for [player].
         * @since 0.1.9
         */
        fun remove(player: UUID) {
            players.remove(player.toString())
        }
    }
}
