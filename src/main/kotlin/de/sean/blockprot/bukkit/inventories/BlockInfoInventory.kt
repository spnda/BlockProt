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
        val friends = handler.friends
        val redstone = handler.redstone

        inventory.clear()
        state.friendResultCache.clear()
        for (i in 0..(friends.size - 1).coerceAtMost(InventoryConstants.doubleLine)) { // Maximum of 2 lines of skulls
            val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(friends[i].name))
            inventory.setItem(
                InventoryConstants.lineLength + i,
                ItemUtil.getItemStack(1, Material.SKELETON_SKULL, offlinePlayer.name)
            )
            state.friendResultCache.add(offlinePlayer)
        }

        if (owner.isNotEmpty()) setPlayerSkull(
            0, Bukkit.getOfflinePlayer(UUID.fromString(owner))
        )
        if (state.friendPage == 0 && friends.size >= InventoryConstants.doubleLine) {
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

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance()!!) { _ ->
            var i = 0
            while (i < InventoryConstants.doubleLine && i < state.friendResultCache.size) {
                setPlayerSkull(InventoryConstants.lineLength + i, state.friendResultCache[i])
                i++
            }
        }
        return inventory
    }
}
