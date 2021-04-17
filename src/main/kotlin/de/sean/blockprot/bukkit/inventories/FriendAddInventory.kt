package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.ArrayList

object FriendAddInventory {
    val INVENTORY_NAME = Strings.getString("inventories.add_friend.name", "Add Friend")
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)

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
        while (i < 9 * 3 - 3 && i < friendsToAdd.size) {
            inv.setItem(i, ItemUtil.getPlayerSkull(friendsToAdd[i]))
            i++
        }
        inv.setItem(9 * 3 - 2, ItemUtil.getItemStack(1, Material.MAP, Strings.SEARCH))
        inv.setItem(9 * 3 - 1, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
        return inv
    }
}
