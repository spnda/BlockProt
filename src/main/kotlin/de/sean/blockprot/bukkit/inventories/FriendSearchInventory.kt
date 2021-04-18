package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.util.Strings
import net.wesjd.anvilgui.AnvilGUI
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

object FriendSearchInventory {
    private val inventoryName = Strings.getString("inventories.friend_search.name", "Search Players")

    private val playerInventories = emptyMap<UUID, Inventory?>().toMutableMap()

    private fun compareStrings(s1: String, s2: String): Double {
        var longer = s1; var shorter = s2
        if (s1.length < s2.length) {
            longer = s2; shorter = s1
        }
        val longerLength = longer.length
        return if (longerLength == 0) 1.0 // They match 100% if both Strings are empty
        else (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) / longerLength.toDouble()
    }

    fun openAnvilInventory(requestingPlayer: Player) {
        AnvilGUI.Builder()
            // .itemRight(ItemUtil.getItemStack(1, Material.PLAYER_HEAD, null, null))
            .onComplete { player: Player, text: String ->
                var players = Bukkit.getOfflinePlayers().toList()

                // Search for players matching
                players = players.filter {
                    // Filter all the players by search criteria.
                    // If the strings are similar by 30%, the strings are considered similar (imo) and should be added.
                    // If they're less than 30% similar, we should still check if it possibly contains the search criteria
                    // and still add that user.
                    when {
                        it.name == null -> false
                        compareStrings(it.name!!, text) > 0.3 -> true
                        else -> it.name!!.contains(text, ignoreCase = true)
                    }
                }

                playerInventories[player.uniqueId] = FriendSearchResultInventory.createInventoryAndFill(player, players)
                return@onComplete AnvilGUI.Response.close()
            }
            .onClose { player ->
                // If the user is closing, the user hasn't completed the translation yet.
                val inv = playerInventories[player.uniqueId]
                if (inv != null) player.openInventory(inv)
                else InventoryState.remove(player.uniqueId)
            }
            .text("Name")
            .title(inventoryName)
            .plugin(BlockProt.instance)
            // .preventClose() // Allow the user to close
            .open(requestingPlayer)
    }
}
