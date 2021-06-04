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

package de.sean.blockprot.bukkit.events

import de.sean.blockprot.TranslationKey
import de.sean.blockprot.Translator
import de.sean.blockprot.bukkit.inventories.BlockLockInventory
import de.sean.blockprot.bukkit.inventories.InventoryState
import de.sean.blockprot.bukkit.nbt.BlockLockHandler
import de.sean.blockprot.bukkit.util.LockUtil
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
        if (!LockUtil.isLockable(event.clickedBlock!!.state.type)) return
        val player = event.player
        when {
            event.action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking -> {
                // The user right clicked and is trying to access the container
                if (event.clickedBlock != null) {
                    val handler = BlockLockHandler(event.clickedBlock as Block)
                    if (!(handler.canAccess(player.uniqueId.toString()) || player.hasPermission(BlockLockHandler.PERMISSION_BYPASS))) {
                        event.isCancelled = true
                        sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION))
                    }
                }
            }
            event.action == Action.RIGHT_CLICK_BLOCK && player.isSneaking && player.hasPermission(BlockLockHandler.PERMISSION_LOCK) -> {
                if (event.item != null) return // Only enter the menu with an empty hand.
                val handler = BlockLockHandler(event.clickedBlock!!)
                event.isCancelled = true
                // Only open the menu if the player is the owner of this block
                // or if this block is not protected or if they have the special permissions.
                if (handler.isNotProtected() ||
                    handler.getOwner() == player.uniqueId.toString() ||
                    event.player.isOp ||
                    player.hasPermission(BlockLockHandler.PERMISSION_INFO) ||
                    player.hasPermission(BlockLockHandler.PERMISSION_ADMIN)
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
