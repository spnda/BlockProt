package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

object UserSettingsInventory : BlockProtInventory {
    override val size = 9 * 1
    override val inventoryName = Strings.getString("inventories.user_settings.name", "User Settings")

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        val nbtEntity = NBTEntity(player).persistentDataContainer
        when (item.type) {
            Material.BARRIER -> {
                // Lock on place button, default value is true
                nbtEntity.setBoolean(
                    LockUtil.LOCK_ON_PLACE_ATTRIBUTE,
                    !nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE)
                )
                val lockOnPlace = nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE)
                Bukkit.getLogger().info(lockOnPlace.toString())
                event.inventory.setItem(
                    0,
                    ItemUtil.getItemStack(
                        1,
                        Material.BARRIER,
                        if (lockOnPlace) Strings.USER_SETTINGS_LOCK_ON_PLACE_DEACTIVATE
                        else Strings.USER_SETTINGS_LOCK_ON_PLACE_ACTIVATE
                    )
                )
            }
            Material.PLAYER_HEAD -> {
                if (state == null) return
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH
                val currentFriends = LockUtil.parseStringList(nbtEntity.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
                val friendsToAdd =
                    FriendAddInventory.filterFriendsList(
                        currentFriends,
                        Bukkit.getOnlinePlayers().toList(),
                        player.uniqueId.toString()
                    )
                val inv = FriendAddInventory.createInventoryAndFill(friendsToAdd)
                player.closeInventory()
                player.openInventory(inv)
                InventoryState.set(player.uniqueId, state)
            }
            Material.ZOMBIE_HEAD -> {
                if (state == null) return
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH
                val currentFriends = LockUtil.parseStringList(nbtEntity.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE))
                val inv = FriendRemoveInventory.createInventoryAndFill(currentFriends)
                player.closeInventory()
                player.openInventory(inv)
            }
            Material.BLACK_STAINED_GLASS_PANE -> player.closeInventory()
            else -> {
            }
        }
        event.isCancelled = true
    }

    fun createInventoryAndFill(player: Player): Inventory {
        val inv = createInventory()
        val nbtEntity = NBTEntity(player).persistentDataContainer
        val lockOnPlace = nbtEntity.getBoolean(LockUtil.LOCK_ON_PLACE_ATTRIBUTE)
        inv.setItem(
            0,
            ItemUtil.getItemStack(
                1,
                Material.BARRIER,
                if (lockOnPlace) Strings.USER_SETTINGS_LOCK_ON_PLACE_DEACTIVATE
                else Strings.USER_SETTINGS_LOCK_ON_PLACE_ACTIVATE
            )
        )
        inv.setItem(
            1,
            ItemUtil.getItemStack(
                1,
                Material.PLAYER_HEAD,
                Strings.BLOCK_LOCK_ADD_FRIENDS
            )
        )
        inv.setItem(
            2,
            ItemUtil.getItemStack(
                1,
                Material.ZOMBIE_HEAD,
                Strings.BLOCK_LOCK_REMOVE_FRIENDS
            )
        )
        inv.setItem(8, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
        return inv
    }
}
