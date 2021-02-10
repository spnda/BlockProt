package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.Strings
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

object FriendSearchInventory {
    private val INVENTORY_NAME = Strings.getString("inventories.friend_search.name", "Search Players")

    private val playerNames = emptyMap<UUID, Inventory?>().toMutableMap()

    fun openAnvilInventory(requestingPlayer: Player) {
        val gui = AnvilGUI.Builder()
            // .itemRight(ItemUtil.getItemStack(1, Material.PLAYER_HEAD, null, null))
            .onComplete { player: Player, text: String ->
                var players = Bukkit.getOfflinePlayers().toList()

                // Search for players matching
                players = players.filter {
                    if (it.name == null) false
                    else it.name!!.contains(text) || it.name.equals(text)
                }

                playerNames[player.uniqueId] = FriendSearchResultInventory.createInventoryAndFill(player, players)
                return@onComplete AnvilGUI.Response.close()
            }
            .onClose { player ->
                // If the user is closing, the user hasn't completed the translation yet.
                val inv = playerNames[player.uniqueId]
                if (inv != null) player.openInventory(inv)
                else LockUtil.remove(player.uniqueId.toString())
            }
            .text("Name")
            .title(INVENTORY_NAME)
            .plugin(BlockProt.instance)
            // .preventClose() // Allow the user to close
            .open(requestingPlayer)
    }
}
