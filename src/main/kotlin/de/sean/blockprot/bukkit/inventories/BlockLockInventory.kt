package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.tr7zw.changeme.nbtapi.NBTTileEntity
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class BlockLockInventory : BlockProtInventory() {
    private var redstone: Boolean = false

    override fun getSize() = InventoryConstants.singleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__BLOCK_LOCK)

    override fun onInventoryClick(event: InventoryClickEvent, state: InventoryState?) {
        if (state?.block == null) return
        val block: Block = state.block
        val item = event.currentItem ?: return

        val player = event.whoClicked as Player
        val inv: Inventory

        when (item.type) {
            in LockUtil.lockableTileEntities, in LockUtil.lockableBlocks -> {
                val doubleChest = getDoubleChest(block, player.world)
                applyChanges(block, player, true) {
                    it.lockBlock(
                        player,
                        player.isOp,
                        if (doubleChest != null) NBTTileEntity(doubleChest) else null
                    )
                }
            }
            Material.REDSTONE, Material.GUNPOWDER -> {
                redstone = !redstone
                setItemStack(
                    1,
                    if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                    if (redstone) TranslationKey.INVENTORIES__REDSTONE__DISALLOW
                    else TranslationKey.INVENTORIES__REDSTONE__ALLOW
                )
            }
            Material.PLAYER_HEAD -> {
                inv = FriendManageInventory().fill(player)
                player.closeInventory()
                player.openInventory(inv)
            }
            Material.OAK_SIGN -> {
                player.closeInventory()
                inv = BlockInfoInventory().fill(player, BlockLockHandler(block))
                player.openInventory(inv)
            }
            else -> exit(player) // This also includes Material.BLACK_STAINED_GLASS_PANE
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState?) {
        if (state != null && state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.block != null) {
            val doubleChest = getDoubleChest(state.block, event.player.world)
            applyChanges(state.block, event.player as Player, false) {
                val ret = it.lockRedstoneForBlock(
                    event.player.uniqueId.toString(),
                    if (doubleChest != null) NBTTileEntity(doubleChest) else null
                )
                redstone = it.getRedstone()
                return@applyChanges ret
            }
        }
    }

    fun fill(player: Player, material: Material, handler: BlockLockHandler): Inventory {
        val playerUuid = player.uniqueId.toString()
        val owner = handler.getOwner()
        redstone = handler.getRedstone()

        if (owner.isEmpty()) {
            setItemStack(
                0,
                material,
                TranslationKey.INVENTORIES__LOCK
            )
        } else if (owner == playerUuid || player.hasPermission(LockUtil.PERMISSION_ADMIN)) {
            setItemStack(
                0,
                material,
                TranslationKey.INVENTORIES__UNLOCK
            )
        }
        if (owner == playerUuid) {
            setItemStack(
                1,
                if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                if (redstone) TranslationKey.INVENTORIES__REDSTONE__DISALLOW
                else TranslationKey.INVENTORIES__REDSTONE__ALLOW
            )
            setItemStack(
                2,
                Material.PLAYER_HEAD,
                TranslationKey.INVENTORIES__FRIENDS__MANAGE
            )
        }
        if (player.isOp ||
            player.hasPermission(LockUtil.PERMISSION_INFO) ||
            player.hasPermission(LockUtil.PERMISSION_ADMIN)
        ) {
            setItemStack(
                InventoryConstants.lineLength - 2,
                Material.OAK_SIGN,
                TranslationKey.INVENTORIES__BLOCK_INFO
            )
        }
        setBackButton()
        return inventory
    }
}
