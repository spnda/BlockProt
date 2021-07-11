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

import de.sean.blockprot.bukkit.TranslationKey
import de.sean.blockprot.bukkit.Translator
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler
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
                (event.inventory.holder as BlockProtInventory)
                    .setItemStack(
                        0,
                        Material.BARRIER,
                        if (lockOnPlace) Translator.get(TranslationKey.INVENTORIES__LOCK_ON_PLACE__DEACTIVATE)
                        else Translator.get(TranslationKey.INVENTORIES__LOCK_ON_PLACE__ACTIVATE)
                    )
            }
            Material.PLAYER_HEAD -> {
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH
                closeAndOpen(player, FriendManageInventory().fill(player))
            }
            else -> closeAndOpen(player, null) // This also includes Material.BLACK_STAINED_GLASS_PANE
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
