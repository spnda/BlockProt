package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.util.ItemUtil
import de.sean.blockprot.util.setBackButton
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
                event.isCancelled = true
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
                event.inventory.setItem(
                    1,
                    ItemUtil.getItemStack(
                        1,
                        if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                        if (redstone) Translator.get(TranslationKey.INVENTORIES__REDSTONE__DISALLOW)
                        else Translator.get(TranslationKey.INVENTORIES__REDSTONE__ALLOW)
                    )
                )
                event.isCancelled = true
            }
            Material.PLAYER_HEAD -> {
                handler = BlockLockHandler(block)
                val owner = handler.getOwner()
                val friendsToAdd = FriendAddInventory.filterFriendsList(handler.getAccess(), Bukkit.getOnlinePlayers().toList(), owner)
                inv = FriendAddInventory.createInventoryAndFill(friendsToAdd)
                player.closeInventory()
                player.openInventory(inv)
                event.isCancelled = true
            }
            Material.ZOMBIE_HEAD -> {
                handler = BlockLockHandler(block)
                val friends = handler.getAccess()
                inv = FriendRemoveInventory.createInventoryAndFill(player, friends)
                player.closeInventory()
                player.openInventory(inv)
                event.isCancelled = true
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
    }

    fun createInventoryAndFill(player: Player, material: Material, handler: BlockLockHandler): Inventory {
        val inv = createInventory()
        val playerUuid = player.uniqueId.toString()
        val owner = handler.getOwner()
        val redstone = handler.getRedstone()
        if (owner.isEmpty()) {
            inv.setItem(
                0,
                ItemUtil.getItemStack(1, material, Translator.get(TranslationKey.INVENTORIES__LOCK))
            )
        } else if (owner == playerUuid || player.hasPermission(LockUtil.PERMISSION_ADMIN)) {
            inv.setItem(
                0,
                ItemUtil.getItemStack(1, material, Translator.get(TranslationKey.INVENTORIES__UNLOCK))
            )
        }
        if (owner == playerUuid) {
            inv.setItem(
                1,
                ItemUtil.getItemStack(
                    1,
                    if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                    if (redstone) Translator.get(TranslationKey.INVENTORIES__REDSTONE__DISALLOW)
                    else Translator.get(TranslationKey.INVENTORIES__REDSTONE__ALLOW)
                )
            )
            inv.setItem(
                2,
                ItemUtil.getItemStack(
                    1,
                    Material.PLAYER_HEAD,
                    Translator.get(TranslationKey.INVENTORIES__FRIENDS__ADD)
                )
            )
            inv.setItem(
                3,
                ItemUtil.getItemStack(
                    1,
                    Material.ZOMBIE_HEAD,
                    Translator.get(TranslationKey.INVENTORIES__FRIENDS__REMOVE)
                )
            )
        }
        if (player.isOp ||
            player.hasPermission(LockUtil.PERMISSION_INFO) ||
            player.hasPermission(LockUtil.PERMISSION_ADMIN)
        ) {
            inv.setItem(
                InventoryConstants.lineLength - 2,
                ItemUtil.getItemStack(
                    1,
                    Material.OAK_SIGN,
                    Translator.get(TranslationKey.INVENTORIES__BLOCK_INFO)
                )
            )
        }
        inv.setBackButton()
        return inv
    }
}
