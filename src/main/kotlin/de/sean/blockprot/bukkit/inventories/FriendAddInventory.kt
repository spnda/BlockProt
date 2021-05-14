package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.FriendModifyAction
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.setBackButton
import de.sean.blockprot.util.setItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.SkullMeta

object FriendAddInventory : BlockFriendModifyInventory {
    override val size = InventoryConstants.tripleLine
    override val inventoryName = Translator.get(TranslationKey.INVENTORIES__FRIENDS__ADD)

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
                val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                val friend = skull.owningPlayer ?: return
                modifyFriendsForAction(state, player, friend, FriendModifyAction.ADD_FRIEND, exit = true)
            }
            Material.MAP -> {
                FriendSearchInventory.openAnvilInventory(player)
            }
            else -> {
                player.closeInventory()
                InventoryState.remove(player.uniqueId)
            }
        }
        event.isCancelled = true
    }

    fun filterFriendsList(current: List<String>, allPlayers: List<Player>, self: String): MutableList<Player> {
        val ret: MutableList<Player> = ArrayList()
        for (player in allPlayers) {
            val playerUuid = player.uniqueId.toString()
            if (!current.contains(playerUuid) && playerUuid != self) ret.add(player)
        }
        return ret
    }

    fun createInventoryAndFill(friendsToAdd: MutableList<Player>): Inventory {
        val inv = createInventory()
        var i = 0
        while (i < InventoryConstants.tripleLine - 3 && i < friendsToAdd.size) {
            inv.setItem(i, ItemUtil.getPlayerSkull(friendsToAdd[i]))
            i++
        }
        inv.setItemStack(
            InventoryConstants.tripleLine - 2,
            Material.MAP,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH
        )
        inv.setBackButton()
        return inv
    }
}
