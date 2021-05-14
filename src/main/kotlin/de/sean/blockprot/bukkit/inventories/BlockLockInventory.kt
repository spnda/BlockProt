package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.util.setBackButton
import de.sean.blockprot.util.setItemStack
import de.tr7zw.nbtapi.NBTTileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

object BlockLockInventory : BlockProtInventory {
    override val size = InventoryConstants.singleLine
    override val inventoryName = Translator.get(TranslationKey.INVENTORIES__BLOCK_LOCK)

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        if (state?.block == null) return
        val block: Block = state.block
        val item = event.currentItem ?: return

        val player = event.whoClicked as Player
        val inv: Inventory
        val handler: BlockLockHandler

        when (item.type) {
            in LockUtil.lockableTileEntities, in LockUtil.lockableBlocks -> {
                val doubleChest = getDoubleChest(block, player.world)
                applyChanges(block, player, exit = true) {
                    it.lockBlock(
                        player,
                        player.isOp,
                        if (doubleChest != null) NBTTileEntity(doubleChest) else null
                    )
                }
            }
            Material.REDSTONE, Material.GUNPOWDER -> {
                val doubleChest = getDoubleChest(block, player.world)
                var redstone = true
                applyChanges(block, player, exit = false) {
                    val ret = it.lockRedstoneForBlock(
                        player.uniqueId.toString(),
                        if (doubleChest != null) NBTTileEntity(doubleChest) else null
                    )
                    redstone = it.getRedstone()
                    return@applyChanges ret
                }
                event.inventory.setItemStack(
                    1,
                    if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                    if (redstone) TranslationKey.INVENTORIES__REDSTONE__DISALLOW
                    else TranslationKey.INVENTORIES__REDSTONE__ALLOW
                )
            }
            Material.PLAYER_HEAD -> {
                handler = BlockLockHandler(block)
                val owner = handler.getOwner()
                val friendsToAdd = FriendAddInventory.filterFriendsList(handler.getAccess(), Bukkit.getOnlinePlayers().toList(), owner)
                inv = FriendAddInventory.createInventoryAndFill(friendsToAdd)
                player.closeInventory()
                player.openInventory(inv)
            }
            Material.ZOMBIE_HEAD -> {
                handler = BlockLockHandler(block)
                val friends = handler.getAccess()
                inv = FriendRemoveInventory.createInventoryAndFill(player, friends)
                player.closeInventory()
                player.openInventory(inv)
            }
            Material.OAK_SIGN -> {
                player.closeInventory()
                inv = BlockInfoInventory.createInventoryAndFill(player, BlockLockHandler(block))
                player.openInventory(inv)
            }
            else -> { // This also includes Material.BLACK_STAINED_GLASS_PANE
                player.closeInventory()
                InventoryState.remove(player.uniqueId)
            }
        }
        event.isCancelled = true
    }

    fun createInventoryAndFill(player: Player, material: Material, handler: BlockLockHandler): Inventory {
        val inv = createInventory()
        val playerUuid = player.uniqueId.toString()
        val owner = handler.getOwner()
        val redstone = handler.getRedstone()
        if (owner.isEmpty()) {
            inv.setItemStack(
                0,
                material,
                TranslationKey.INVENTORIES__LOCK
            )
        } else if (owner == playerUuid || player.hasPermission(LockUtil.PERMISSION_ADMIN)) {
            inv.setItemStack(
                0,
                material,
                TranslationKey.INVENTORIES__UNLOCK
            )
        }
        if (owner == playerUuid) {
            inv.setItemStack(
                1,
                if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                if (redstone) TranslationKey.INVENTORIES__REDSTONE__DISALLOW
                else TranslationKey.INVENTORIES__REDSTONE__ALLOW
            )
            inv.setItemStack(
                2,
                Material.PLAYER_HEAD,
                TranslationKey.INVENTORIES__FRIENDS__ADD
            )
            inv.setItemStack(
                3,
                Material.ZOMBIE_HEAD,
                TranslationKey.INVENTORIES__FRIENDS__REMOVE
            )
        }
        if (player.isOp ||
            player.hasPermission(LockUtil.PERMISSION_INFO) ||
            player.hasPermission(LockUtil.PERMISSION_ADMIN)
        ) {
            inv.setItemStack(
                InventoryConstants.lineLength - 2,
                Material.OAK_SIGN,
                TranslationKey.INVENTORIES__BLOCK_INFO
            )
        }
        inv.setBackButton()
        return inv
    }
}
