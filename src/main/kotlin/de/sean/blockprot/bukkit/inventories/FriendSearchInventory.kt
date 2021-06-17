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
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

object FriendSearchInventory {
    private val inventoryName = Translator.get(TranslationKey.INVENTORIES__FRIENDS__SEARCH)

    private val playerInventories = emptyMap<UUID, Inventory?>().toMutableMap()

    fun openAnvilInventory(requestingPlayer: Player) {
        AnvilGUI.Builder()
            .onComplete { player: Player, searchQuery: String ->
                playerInventories[player.uniqueId] = FriendSearchResultInventory().fill(player, searchQuery)
                return@onComplete AnvilGUI.Response.openInventory(playerInventories[player.uniqueId])
            }
            .text("Name")
            .title(inventoryName)
            .plugin(BlockProt.instance)
            // .preventClose() // Allow the user to close
            .open(requestingPlayer)
    }
}
