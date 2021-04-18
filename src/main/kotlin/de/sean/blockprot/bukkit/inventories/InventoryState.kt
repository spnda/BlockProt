package de.sean.blockprot.bukkit.inventories

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
