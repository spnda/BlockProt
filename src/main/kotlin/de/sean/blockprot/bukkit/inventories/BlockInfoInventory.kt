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

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.util.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import java.util.*

class BlockInfoInventory : BlockProtInventory() {
    override fun getSize() = InventoryConstants.tripleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__BLOCK_INFO)

    override fun onClick(event: InventoryClickEvent, state: InventoryState) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BLACK_STAINED_GLASS_PANE -> {
                player.closeInventory()
                if (state.block == null) return
                val handler = BlockNBTHandler(state.block)
                val inv = BlockLockInventory().fill(player, state.block.state.type, handler)
                player.openInventory(inv)
            }
            Material.CYAN_STAINED_GLASS_PANE -> {
                if (state.friendPage >= 1) {
                    state.friendPage = state.friendPage - 1

                    player.closeInventory()
                    if (state.block != null) player.openInventory(fill(player, BlockNBTHandler(state.block)))
                }
            }
            Material.BLUE_STAINED_GLASS_PANE -> {
                val lastFriendInInventory = inventory.getItem(InventoryConstants.tripleLine - 1)
                if (lastFriendInInventory != null && lastFriendInInventory.amount == 0) {
                    // There's an item in the last slot => The page is fully filled up, meaning we should go to the next page.
                    state.friendPage = state.friendPage + 1

                    player.closeInventory()
                    if (state.block != null) player.openInventory(fill(player, BlockNBTHandler(state.block)))
                }
            }
            else -> {
            }
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState) {}

    fun fill(player: Player, handler: BlockNBTHandler): Inventory {
        val state = InventoryState.get(player.uniqueId) ?: return inventory
        val owner = handler.owner
        val access = handler.access
        val redstone = handler.redstone

        inventory.clear()
        state.friendResultCache.clear()
        for (i in 0..(access.size - 1).coerceAtMost(InventoryConstants.doubleLine)) { // Maximum of 2 lines of skulls
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(access[i]))
            inventory.setItem(
                InventoryConstants.lineLength + i,
                ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name)
            )
            state.friendResultCache.add(offlinePlayer)
        }

        if (owner.isNotEmpty()) inventory.setItem(
            0,
            ItemUtil.getPlayerSkull(Bukkit.getOfflinePlayer(UUID.fromString(owner)))
        )
        if (state.friendPage == 0 && access.size >= InventoryConstants.doubleLine) {
            setItemStack(
                InventoryConstants.lineLength - 3,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE,
            )
            setItemStack(
                InventoryConstants.lineLength - 2,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE,
            )
        }
        setItemStack(
            1,
            if (redstone) Material.REDSTONE else Material.GUNPOWDER,
            if (redstone) TranslationKey.INVENTORIES__REDSTONE__ALLOWED
            else TranslationKey.INVENTORIES__REDSTONE__DISALLOWED
        )
        setBackButton(InventoryConstants.lineLength - 1)

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance) { _ ->
            var i = 0
            while (i < InventoryConstants.doubleLine && i < state.friendResultCache.size) {
                val skull = ItemUtil.getPlayerSkull(state.friendResultCache[i])
                inventory.setItem(InventoryConstants.lineLength + i, skull)
                i++
            }
        }
        return inventory
    }
}
