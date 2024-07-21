/*
 * Copyright (C) 2021 - 2024 spnda
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
import de.sean.blockprot.bukkit.Permissions;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import de.sean.blockprot.bukkit.inventories.BlockProtInventory;
import de.sean.blockprot.bukkit.inventories.InventoryState;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InventoryEventListener implements Listener {
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state != null) {
            // We have some sort of inventory state, so we'll assume the player is currently in
            // some of our inventories. We'll check from which inventory the clicked item actually is,
            // so that the onClick method is only called for menu clicks, but we'll still cancel all
            // clicks in the players inventory.
            InventoryHolder holder = event.getInventory().getHolder();
            if (holder instanceof BlockProtInventory) {
                // While getInventory always returns the top inventory, getClickedInventory returns the
                // inventory in which this event occurred.
                final var clickedInventory = event.getClickedInventory();
                if (clickedInventory != null && clickedInventory.getHolder() instanceof BlockProtInventory bpInventory) {
                    bpInventory.onClick(event, state);
                } else {
                    event.setCancelled(true); // Don't allow interaction in a menu.
                }

                return;
            }
        }

        // No state, let's check if they're in some block inventory.
        try {
            // Casting null does not trigger a ClassCastException.
            if (event.getInventory().getHolder() == null) return;
            final InventoryHolder holder = event.getInventory().getHolder();

            Block block;
            if (holder instanceof BlockInventoryHolder blockHolder) {
                block = blockHolder.getBlock();
            } else if (holder instanceof DoubleChest doubleChestHolder) {
                block = doubleChestHolder.getLocation().getBlock();
            } else {
                return;
            }

            if (BlockProt.getDefaultConfig().isLockable(block.getType())) {
                // Ok, we have a lockable block, check if they can write anything to this.
                // TODO: Implement a Cache for this lookup, it seems to be quite expensive.
                //       We should probably use a MultiMap, or implement our own Key that
                //       can use multiple key objects, a Block and Player in this case.
                BlockNBTHandler handler = new BlockNBTHandler(block);
                String playerUuid = player.getUniqueId().toString();

                if (handler.isProtected() && !handler.isOwner(playerUuid)) {
                    final var friend = handler.getFriend(playerUuid);
                    if (friend.isPresent()) {
                        if (!friend.get().canWrite()) {
                            event.setCancelled(true);
                        } else if (!friend.get().canRead()) {
                            event.setCancelled(true);
                            Bukkit.getScheduler().runTask(BlockProt.getInstance(), player::closeInventory);
                        }
                    } else {
                        // The player is not a friend and not the owner; they shouldn't have
                        // access anyway.
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTask(BlockProt.getInstance(), player::closeInventory);
                    }
                }
            }
        } catch (ClassCastException e) {
            // It's not a block, and it's therefore also not lockable.
            // This is probably some other custom inventory from another
            // plugin, or possibly some entity inventory, e.g. villagers.
        }
    }

    @EventHandler
    public void onLecternClick(@NotNull PlayerTakeLecternBookEvent event) {
        final var handler = new BlockNBTHandler(event.getLectern().getBlock());
        final var uuid = event.getPlayer().getUniqueId();

        if (handler.isProtected() && !handler.isOwner(uuid)) {
            // The player taking the book is not the owner.
            final var friend = handler.getFriend(uuid.toString());
            if (friend.isPresent()) {
                if (!friend.get().canWrite()) {
                    event.setCancelled(true);
                } else if (!friend.get().canRead()) {
                    // Not a friend who should be able to access the inventory
                    event.setCancelled(true);
                    event.getPlayer().closeInventory();
                }
            } else {
                // Not a friend; close the inventory
                event.setCancelled(true);
                event.getPlayer().closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BlockProtInventory) {
            ((BlockProtInventory) holder).onClose(event, state);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        // Double-check any inventories and close them if permissions are not valid.
        String playerUuid = event.getPlayer().getUniqueId().toString();
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BlockProtInventory) {
            // We've got one of our inventories, check the inventory state if the player can access
            // the block. Also fixes issues when the block gets destroyed.
            InventoryState state = InventoryState.get(playerUuid);
            if (state == null || state.getBlock() == null) return;
            try {
                BlockNBTHandler handler = new BlockNBTHandler(state.getBlock());
                Optional<FriendHandler> friend = handler.getFriend(playerUuid);
                if (!(handler.isNotProtected()
                    || handler.isOwner(playerUuid)
                    || (friend.isPresent() && friend.get().isManager())
                    || event.getPlayer().hasPermission(Permissions.ADMIN.key())
                    || event.getPlayer().hasPermission(Permissions.INFO.key()))) {
                    event.setCancelled(true);
                    sendMessage(event.getPlayer(), Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                }
            } catch (RuntimeException ignored) {
            }
        } else if ((holder instanceof Container || holder instanceof DoubleChest)
            && event.getPlayer() instanceof Player player) {
            Block block;
            if (holder instanceof Container container) {
                block = container.getBlock();
            } else {
                block = ((DoubleChest) holder).getLocation().getBlock();
            }

            if (BlockProt.getDefaultConfig().isLockable(block.getType())) {
                BlockAccessEvent accessEvent = new BlockAccessEvent(block, (Player) event.getPlayer());
                Bukkit.getPluginManager().callEvent(accessEvent);
                if (accessEvent.isCancelled()) {
                    event.setCancelled(true);
                    sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                } else {
                    BlockNBTHandler handler = new BlockNBTHandler(block);
                    if (!accessEvent.shouldBypassProtections()
                            && !(handler.canAccess(player.getUniqueId().toString()) || player.hasPermission(Permissions.BYPASS.key()))) {
                        event.setCancelled(true);
                        sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                    }
                }
            }
        }
    }

    private void sendMessage(@NotNull HumanEntity player, @NotNull String component) {
        if (!(player instanceof Player)) return;
        ((Player) player).spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText(component)
        );
    }
}
