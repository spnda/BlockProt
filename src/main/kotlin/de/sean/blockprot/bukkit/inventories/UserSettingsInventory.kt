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
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler
import de.sean.blockprot.bukkit.util.ItemUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class UserSettingsInventory : BlockProtInventory() {
    override fun getSize() = InventoryConstants.singleLine
    override fun getTranslatedInventoryName() = Translator.get(TranslationKey.INVENTORIES__USER_SETTINGS)

    override fun onClick(event: InventoryClickEvent, state: InventoryState) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        when (item.type) {
            Material.BARRIER -> {
                // Lock on place button, default value is true
                val settingsHandler = PlayerSettingsHandler(player)
                val lockOnPlace = !settingsHandler.lockOnPlace
                settingsHandler.lockOnPlace = lockOnPlace
                event.inventory.setItem(
                    0,
                    ItemUtil.getItemStack(
                        1,
                        Material.BARRIER,
                        if (lockOnPlace) Translator.get(TranslationKey.INVENTORIES__LOCK_ON_PLACE__DEACTIVATE)
                        else Translator.get(TranslationKey.INVENTORIES__LOCK_ON_PLACE__ACTIVATE)
                    )
                )
            }
            Material.PLAYER_HEAD -> {
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH
                val inv = FriendManageInventory().fill(player)
                player.closeInventory()
                player.openInventory(inv)
            }
            else -> exit(player) // This also includes Material.BLACK_STAINED_GLASS_PANE
        }
        event.isCancelled = true
    }

    override fun onClose(event: InventoryCloseEvent, state: InventoryState) {}

    fun fill(player: Player): Inventory {
        val settingsHandler = PlayerSettingsHandler(player)
        val lockOnPlace = settingsHandler.lockOnPlace
        setItemStack(
            0,
            Material.BARRIER,
            if (lockOnPlace) TranslationKey.INVENTORIES__LOCK_ON_PLACE__DEACTIVATE
            else TranslationKey.INVENTORIES__LOCK_ON_PLACE__ACTIVATE
        )
        setItemStack(
            1,
            Material.PLAYER_HEAD,
            TranslationKey.INVENTORIES__FRIENDS__MANAGE
        )
        setBackButton()
        return inventory
    }
}
