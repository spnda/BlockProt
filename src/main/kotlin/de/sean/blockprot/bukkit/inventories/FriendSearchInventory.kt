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
