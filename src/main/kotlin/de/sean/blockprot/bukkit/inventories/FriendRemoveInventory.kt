package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.FriendModifyAction
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.setBackButton
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.util.*

object FriendRemoveInventory : BlockFriendModifyInventory {
    override val size = InventoryConstants.tripleLine
    override val inventoryName: String = Translator.get(TranslationKey.INVENTORIES__FRIENDS__REMOVE)

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                if (state == null) return
                exitModifyInventory(player, state)
            }
            Material.PLAYER_HEAD -> {
                if (state == null) return
                val index = findItemIndex(event.inventory, item)
                val friend = state.friendResultCache[index]
                modifyFriendsForAction(state, player, friend, FriendModifyAction.REMOVE_FRIEND, exit = true)
            }
            else -> {
                player.closeInventory()
                InventoryState.remove(player.uniqueId)
            }
        }
        event.isCancelled = true
    }

    fun createInventoryAndFill(player: Player, friendsToRemove: List<String>): Inventory {
        val inv = createInventory()
        val state = InventoryState.get(player.uniqueId) ?: return inv

        state.friendResultCache.clear()
        for (i in friendsToRemove.indices) {
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(friendsToRemove[i]))
            inv.setItem(i, ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name))
            state.friendResultCache.add(offlinePlayer)
        }
        inv.setBackButton()

        // Get the skulls asynchronously and add them one after each other.
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            while (i < InventoryConstants.tripleLine - 2 && i < state.friendResultCache.size) {
                val skull = ItemUtil.getPlayerSkull(state.friendResultCache[i])
                inv.setItem(i, skull)
                i++
            }
        }

        return inv
    }
}
