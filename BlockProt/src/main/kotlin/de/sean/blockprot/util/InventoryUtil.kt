package de.sean.blockprot.util

import de.sean.blockprot.bukkit.inventories.BlockLockInventory
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * Creates the base [BlockLockInventory] inventory with given information.
 */
fun createBaseInventory(player: Player, material: Material, handler: BlockLockHandler): Inventory {
    val inv = BlockLockInventory.createInventory()
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
                if (redstone) Material.GUNPOWDER else Material.REDSTONE,
                if (redstone) Strings.BLOCK_LOCK_REDSTONE_ACTIVATE
                else Strings.BLOCK_LOCK_REDSTONE_DEACTIVATE
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
