package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.Strings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object BlockLockInventory {
    val INVENTORY_NAME = Strings.getString("inventories.block_lock.name", "Block Lock")
    fun createInventory() = Bukkit.createInventory(null, 9 * 1, INVENTORY_NAME)

    fun createInventoryAndFill(player: Player, material: Material, handler: BlockLockHandler): Inventory {
        val inv = createInventory()
        val playerUuid = player.uniqueId.toString()
        val owner = handler.getOwner()
        val redstone = handler.getRedstone()
        if (owner == playerUuid || player.isOp || player.hasPermission(Strings.BLOCKPROT_ADMIN))
            inv.setItem(0, ItemUtil.getItemStack(1, material, Strings.UNLOCK))
        if (owner == playerUuid) {
            inv.setItem(
                1,
                ItemUtil.getItemStack(
                    1,
                    if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                    if (redstone) Strings.BLOCK_LOCK_REDSTONE_DEACTIVATE
                    else Strings.BLOCK_LOCK_REDSTONE_ACTIVATE
                )
            )
            inv.setItem(2, ItemUtil.getItemStack(1, Material.PLAYER_HEAD, Strings.BLOCK_LOCK_ADD_FRIENDS))
            inv.setItem(3, ItemUtil.getItemStack(1, Material.ZOMBIE_HEAD, Strings.BLOCK_LOCK_REMOVE_FRIENDS))
        }
        if (player.isOp || player.hasPermission(Strings.BLOCKPROT_INFO) || player.hasPermission(Strings.BLOCKPROT_ADMIN))
            inv.setItem(7, ItemUtil.getItemStack(1, Material.OAK_SIGN, Strings.BLOCK_LOCK_INFO))
        inv.setItem(8, ItemUtil.getItemStack(1, Material.BLACK_STAINED_GLASS_PANE, Strings.BACK))
        return inv
    }
}
