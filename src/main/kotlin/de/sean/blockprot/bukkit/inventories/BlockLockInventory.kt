/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.nbt.LockUtil
import de.sean.blockprot.bukkit.nbt.LockUtil.getDoubleChest
import de.sean.blockprot.bukkit.nbt.NBTHandler
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

    override fun onClick(event: InventoryClickEvent, state: InventoryState) {
        if (state.block == null) return
        val block: Block = state.block
        val item = event.currentItem ?: return

        val player = event.whoClicked as Player
        val inv: Inventory

        when {
            LockUtil.isLockable(item.type) -> {
                val doubleChest = getDoubleChest(block, player.world)
                applyChanges(block, player, true, true) {
                    it.lockBlock(
                        player,
                        player.isOp,
                        if (doubleChest != null) NBTTileEntity(doubleChest) else null
                    )
                }
            }
            item.type == Material.REDSTONE || item.type == Material.GUNPOWDER -> {
                redstone = !redstone
                setItemStack(
                    1,
                    if (redstone) Material.REDSTONE else Material.GUNPOWDER,
                    if (redstone) TranslationKey.INVENTORIES__REDSTONE__DISALLOW
                    else TranslationKey.INVENTORIES__REDSTONE__ALLOW
                )
            }
            item.type == Material.PLAYER_HEAD -> {
                inv = FriendManageInventory().fill(player)
                player.closeInventory()
                player.openInventory(inv)
            }
            item.type == Material.OAK_SIGN -> {
                player.closeInventory()
                inv = BlockInfoInventory().fill(player, BlockNBTHandler(block))
                player.openInventory(inv)
            }
            else -> exit(player) // This also includes Material.BLACK_STAINED_GLASS_PANE
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState) {
        if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.block != null) {
            val doubleChest = getDoubleChest(state.block, event.player.world)
            applyChanges(state.block, event.player as Player, false, false) {
                return@applyChanges it.lockRedstoneForBlock(
                    event.player.uniqueId.toString(),
                    if (doubleChest != null) NBTTileEntity(doubleChest) else null,
                    redstone,
                )
            }
        }
    }

    fun fill(player: Player, material: Material, handler: BlockNBTHandler): Inventory {
        val playerUuid = player.uniqueId.toString()
        val owner = handler.owner
        redstone = handler.redstone

        if (owner.isEmpty()) {
            setItemStack(
                0,
                material,
                TranslationKey.INVENTORIES__LOCK
            )
        } else if (owner == playerUuid || player.hasPermission(NBTHandler.PERMISSION_ADMIN)) {
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
        if (owner.isNotEmpty() && (
            player.isOp ||
                player.hasPermission(NBTHandler.PERMISSION_INFO) ||
                player.hasPermission(NBTHandler.PERMISSION_ADMIN)
            )
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
