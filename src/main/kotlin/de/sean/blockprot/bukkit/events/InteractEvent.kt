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
package de.sean.blockprot.bukkit.events

import de.sean.blockprot.BlockProt
import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.inventories.BlockLockInventory
import de.sean.blockprot.bukkit.inventories.InventoryState
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler
import de.sean.blockprot.bukkit.nbt.NBTHandler
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

open class InteractEvent : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    open fun playerInteract(event: PlayerInteractEvent) {
        if (event.clickedBlock == null) return
        if (!BlockProt.defaultConfig.isLockable(event.clickedBlock!!.state.type)) return
        val player = event.player
        when {
            event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking -> {
                // The user right clicked and is trying to access the container
                if (event.clickedBlock != null) {
                    val handler = BlockNBTHandler(event.clickedBlock as Block)
                    if (!(handler.canAccess(player.uniqueId.toString()) || player.hasPermission(NBTHandler.PERMISSION_BYPASS))) {
                        event.isCancelled = true
                        sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
                    }
                }
            }
            event.action == Action.RIGHT_CLICK_BLOCK && player.isSneaking && player.hasPermission(NBTHandler.PERMISSION_LOCK) -> {
                if (event.item != null) return // Only enter the menu with an empty hand.
                val handler = BlockNBTHandler(event.clickedBlock!!)
                event.isCancelled = true
                // Only open the menu if the player is the owner of this block
                // or if this block is not protected or if they have the special permissions.
                if (handler.isNotProtected ||
                    handler.owner == player.uniqueId.toString() ||
                    event.player.isOp ||
                    player.hasPermission(BlockNBTHandler.PERMISSION_INFO) ||
                    player.hasPermission(BlockNBTHandler.PERMISSION_ADMIN)
                ) {
                    val state = InventoryState(event.clickedBlock!!)
                    state.friendSearchState = InventoryState.FriendSearchState.FRIEND_SEARCH
                    InventoryState.set(player.uniqueId, state)
                    val inv = BlockLockInventory().fill(player, event.clickedBlock!!.type, handler)
                    player.openInventory(inv)
                } else {
                    sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
                }
            }
        }
    }

    private fun sendMessage(player: Player, component: String) {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            *TextComponent.fromLegacyText(component)
        )
    }
}
