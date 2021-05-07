package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

object FriendSearchInventory {
    private val inventoryName = Translator.get(TranslationKey.INVENTORIES__FRIENDS__SEARCH)

    private val playerInventories = emptyMap<UUID, Inventory?>().toMutableMap()

    fun openAnvilInventory(requestingPlayer: Player) {
        AnvilGUI.Builder()
            .onComplete { player: Player, searchQuery: String ->
                playerInventories[player.uniqueId] = FriendSearchResultInventory.createInventoryAndFill(player, searchQuery)
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
