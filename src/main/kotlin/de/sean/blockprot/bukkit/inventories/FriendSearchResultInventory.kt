package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.applyToDoor
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.bukkit.nbt.LockUtil.parseStringList
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTEntity
import de.tr7zw.nbtapi.NBTTileEntity
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object FriendSearchResultInventory : BlockProtInventory {
    override val size = 9 * 3
    override val inventoryName = Strings.getString("inventories.friend_search_result.name", "Players found")

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                // Go back to the search if nothing was found
                FriendSearchInventory.openAnvilInventory(player)
            }
            Material.PLAYER_HEAD -> {
                if (state == null) return
                val skull = item.itemMeta as SkullMeta? ?: return // Generic player head?
                val friend = skull.owningPlayer?.uniqueId.toString()
                when (state.friendSearchState) {
                    InventoryState.FriendSearchState.FRIEND_SEARCH -> {
                        if (state.block == null) return
                        val handler = BlockLockHandler(state.block)
                        val doubleChest = getDoubleChest(state.block, player.world)
                        val ret = handler.addFriend(
                            player.uniqueId.toString(),
                            friend,
                            if (doubleChest != null) NBTTileEntity(doubleChest) else null
                        )
                        if (ret.success) {
                            applyToDoor(handler, state.block)
                            player.spigot()
                                .sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(ret.message))
                        }
                        player.closeInventory()
                    }
                    InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH -> {
                        val playerNBT = NBTEntity(player).persistentDataContainer
                        var currentFriendList = parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
                        currentFriendList = currentFriendList.plus(friend)
                        playerNBT.setString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE, currentFriendList.toString())
                        player.closeInventory()
                    }
                }
            }
            Material.BARRIER -> {
                player.closeInventory()
            }
            else -> {}
        }
        event.isCancelled = true
    }

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
                parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
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
