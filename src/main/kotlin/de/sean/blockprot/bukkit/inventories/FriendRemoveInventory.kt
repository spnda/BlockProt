package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

object FriendRemoveInventory {
    val INVENTORY_NAME = Strings.getString("inventories.remove_friend.name", "Remove Friend")
    fun createInventory() = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)

    fun createInventoryAndFill(friendsToRemove: List<String>): Inventory {
        val inv = createInventory()
        // Get the skulls asynchronously and add them one after each other.
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            val skulls: MutableList<ItemStack> = ArrayList()
            while (i < 9 * 3 - 2 && i < friendsToRemove.size) {
                val skull = ItemUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(friendsToRemove[i])))
                skulls.add(skull)
                i++
            }
            Bukkit.getScheduler().runTask(BlockProt.instance) { _ ->
                for (skull in skulls.indices)
                    inv.setItem(skull, skulls[skull])
            }
        }
        inv.setItem(9 * 3 - 1, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
        return inv
    }
}
