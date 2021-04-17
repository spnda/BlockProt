package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import de.tr7zw.nbtapi.NBTEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object UserSettingsInventory {
    val INVENTORY_NAME = Strings.getString("inventories.user_settings.name", "User Settings")
    fun createInventory() = Bukkit.createInventory(null, 9 * 1, INVENTORY_NAME)

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
