package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object FriendSearchResultInventory {
    val INVENTORY_NAME = Strings.getString("inventories.friend_search_result.name", "Players found")
    private fun createInventory() = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)

    fun createInventoryAndFill(player: Player, players: List<OfflinePlayer>): Inventory? {
        val state = InventoryState.get(player.uniqueId) ?: return null

        // The already existing friends we want to add to.
        val friends: List<String> = when (state.friendSearchState) {
            InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                val block = state.block ?: return null
                val handler = BlockLockHandler(block)
                handler.getAccess()
            }
            InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                val playerNBT = NBTEntity(player).persistentDataContainer
                BlockLockHandler.parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
            }
        }

        // To not delay when the inventory opens, we'll asynchronously get the items after the inventory has been opened
        // and later add them to the inventory.
        val inv = createInventory()
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            val items: MutableList<ItemStack> = ArrayList()
            // Only show the 9 * 3 - 2 most relevant players. Don't show any more.
            var playersIndex = 0
            val max = players.size.coerceAtMost(9 * 3 - 2)
            while (playersIndex < max) {
                // Only add to the inventory if this is not a friend (yet)
                if (!friends.contains(players[playersIndex].uniqueId.toString()) && players[playersIndex].uniqueId != player.uniqueId) {
                    items.add(ItemUtil.getPlayerSkull(players[playersIndex]))
                }
                playersIndex += 1
            }
            Bukkit.getScheduler().runTask(BlockProt.instance) { _ ->
                for (item in items.indices) {
                    inv.setItem(item, items[item])
                }
            }
        }
        inv.setItem(9 * 3 - 1, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, null, null))
        return inv
    }
}
