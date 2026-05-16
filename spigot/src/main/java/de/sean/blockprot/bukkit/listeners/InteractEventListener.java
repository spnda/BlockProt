/*
 * Copyright (C) 2021 - 2025 spnda
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

import de.sean.blockprot.bukkit.*;
import de.sean.blockprot.bukkit.events.BlockAccessEvent;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;

public class InteractEventListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerInteract(PlayerInteractEvent event) {
        // The InsaneShops plugin uses this weird FakeEvent event that inherits from PlayerInteractEvent.
        // We don't want to trigger on that interact event, so here's this check.
        if (event.getClass().getName().equals("Lme.TechsCode.InsaneShops.utilities.FakeEvent;"))
            return;

        if (event.getClickedBlock() == null) return;
        if (BlockProt.getDefaultConfig().isWorldExcluded(event.getClickedBlock().getWorld())) return;
        if (!BlockProt.getDefaultConfig().isLockable(event.getClickedBlock().getState().getType())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            BlockAccessEvent accessEvent = new BlockAccessEvent(event.getClickedBlock(), player);
            Bukkit.getPluginManager().callEvent(accessEvent);
            if (accessEvent.isCancelled()) {
                event.setCancelled(true);
                sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
            } else {
                BlockNBTHandler handler = new BlockNBTHandler(event.getClickedBlock());
                if (!(handler.canAccess(player.getUniqueId().toString()) || player.hasPermission(Permissions.BYPASS.key()))) {
                    event.setCancelled(true);
                    sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                    // Registrar intento fallido en el audit log
                    de.sean.blockprot.bukkit.audit.AuditLogger audit = BlockProt.getAuditLogger();
                    if (audit != null) {
                        audit.log(player.getUniqueId(), player.getName(), event.getClickedBlock().getLocation(),
                            de.sean.blockprot.bukkit.audit.AuditLogger.Action.ACCESS_DENIED);
                    }
                } else if (event.getClickedBlock().getType() == Material.LECTERN && !handler.isOwner(player.getUniqueId())) {
                    // With Lecterns you place the books by interacting with the block. canAccess will return true because the
                    // player has the READ permission, but this should not be allowed in this case. In the case that the player
                    // wants to take the book from the lectern (hasBook returns true) we already listen for PlayerTakeLecternBookEvent.
                    final var lectern = (Lectern)event.getClickedBlock().getBlockData();
                    if (!lectern.hasBook()) {
                        final var friend = handler.getFriend(player.getUniqueId().toString());
                        if (friend.isEmpty() || !friend.get().canWrite()) {
                            // The player cannot write and therefore is not allowed to place books into the Lectern.
                            event.setCancelled(true);
                            sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                        }
                    }
                } else if (!(new PlayerSettingsHandler(player).hasPlayerInteractedWithMenu())) {
                    Long timestamp = LockHintMessageCooldown.getTimestamp(player);
                    if (timestamp == null || timestamp < System.currentTimeMillis() - (BlockProt.getDefaultConfig().getLockHintCooldown() * 1000)) { // 10 seconds in milliseconds
                        // If they can access the block we'll notify them that they could
                        // potentially lock their blocks.
                        String message = Translator.get(TranslationKey.MESSAGES__LOCK_HINT);
                        if (!message.isEmpty()) {
                            LockHintMessageCooldown.setTimestamp(player);
                            var tooltip = Translator.get(TranslationKey.MESSAGES__HINT_HOVER_TEXT);
                            sendEventsMessage(player, message, true,
                                "/blockprot disablehints", tooltip.isEmpty() ? null : tooltip);
                        }
                    }
                }
            }
        } else {
            if (event.hasItem()) return; // Only enter the menu with an empty hand.
            event.setCancelled(true);

            if (!player.hasPermission(Permissions.LOCK.key())) {
                sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
                return;
            }

            Inventory inv = BlockProtAPI.getInstance().getLockInventoryForBlock(event.getClickedBlock(), player);
            if (inv == null) {
                sendMessage(player, Translator.get(TranslationKey.MESSAGES__NO_PERMISSION));
            } else {
                new PlayerSettingsHandler(player).setHasPlayerInteractedWithMenu(true);
                player.openInventory(inv);
            }
        }
    }

    private void sendMessage(@NotNull Player player, @NotNull String text) {
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private void sendMessage(@NotNull Player player, @NotNull String text, boolean asChat) {
        var comp = LegacyComponentSerializer.legacySection().deserialize(text);
        if (asChat) player.sendMessage(comp);
        else player.sendActionBar(comp);
    }

    private void sendEventsMessage(@NotNull Player player, @NotNull String text, boolean asChat, @Nullable String command, @Nullable String tooltip) {
        var comp = Component.text(text);
        if (command != null) comp = comp.clickEvent(ClickEvent.runCommand(command));
        if (tooltip != null) comp = comp.hoverEvent(HoverEvent.showText(Component.text(tooltip)));
        if (asChat) player.sendMessage(comp);
        else player.sendActionBar(comp);
    }

    private static class LockHintMessageCooldown {
        private static final HashMap<Player, Long> timestamps = new HashMap<>();

        public static void setTimestamp(final @NotNull Player player) {
            timestamps.put(player, System.currentTimeMillis());
        }

        @Nullable
        public static Long getTimestamp(final @NotNull Player player) {
            return timestamps.get(player);
        }
    }
}
