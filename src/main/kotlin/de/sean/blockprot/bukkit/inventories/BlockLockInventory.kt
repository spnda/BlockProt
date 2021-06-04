/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.sean.blockprot.bukkit.inventories

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.util.LockUtil
import de.sean.blockprot.bukkit.util.LockUtil.getDoubleChest
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
                applyChanges(block, player, true) {
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
                inv = BlockInfoInventory().fill(player, BlockLockHandler(block))
                player.openInventory(inv)
            }
            else -> exit(player) // This also includes Material.BLACK_STAINED_GLASS_PANE
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState) {
        if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.block != null) {
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
        } else if (owner == playerUuid || player.hasPermission(BlockLockHandler.PERMISSION_ADMIN)) {
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
            player.hasPermission(BlockLockHandler.PERMISSION_INFO) ||
            player.hasPermission(BlockLockHandler.PERMISSION_ADMIN)
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
