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

package de.sean.blockprot.bukkit.listeners;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.BlockProtAPI;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.NBTHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class InteractEventListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getClickedBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getClickedBlock().getState().getType())) return;
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
            BlockAccessEvent accessEvent = new BlockAccessEvent(event.getClickedBlock(), player);
            Bukkit.getPluginManager().callEvent(accessEvent);
            if (accessEvent.isCancelled()) {
                event.setCancelled(true);
                sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
            } else {
                BlockNBTHandler handler = new BlockNBTHandler(event.getClickedBlock());
                if (!(handler.canAccess(player.getUniqueId().toString()) || player.hasPermission(NBTHandler.PERMISSION_BYPASS))) {
                    event.setCancelled(true);
                    sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking() && player.hasPermission(NBTHandler.PERMISSION_LOCK)) {
            if (event.hasItem()) return; // Only enter the menu with an empty hand.

            event.setCancelled(true);
            Inventory inv = BlockProtAPI.getInstance().getLockInventoryForBlock(event.getClickedBlock(), player);
            if (inv == null) {
                sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
            } else {
                player.openInventory(inv);
            }
        }
    }

    private void sendMessage(@NotNull Player player, @NotNull String component) {
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText(component)
        );
    }
}
