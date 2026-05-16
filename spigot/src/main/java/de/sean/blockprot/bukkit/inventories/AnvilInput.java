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

package de.sean.blockprot.bukkit.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Opens a real server-side anvil (backed by AnvilMenu) so that
 * ServerboundRenameItemPacket is properly processed and
 * AnvilView.getRenameText() returns the text the player typed.
 *
 * Virtual inventories created via Bukkit.createInventory(null, ANVIL, ...)
 * do not have a backing AnvilMenu, so getRenameText() always returns null.
 */
public final class AnvilInput implements Listener {

    private static final int OUTPUT_SLOT = 2;

    private final UUID playerUuid;
    private final @Nullable Consumer<String> onConfirm;
    private boolean consumed = false;

    private AnvilInput(
        @NotNull Player player,
        @NotNull Plugin plugin,
        @NotNull String initialText,
        @Nullable Consumer<String> onConfirm
    ) {
        this.playerUuid = player.getUniqueId();
        this.onConfirm = onConfirm;

        // Register before opening so PrepareAnvilEvent is caught immediately.
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Open a REAL server-side anvil. This creates an AnvilMenu on the server
        // so ServerboundRenameItemPacket is processed and getRenameText() works.
        // openAnvil creates a real server-side AnvilMenu — required for getRenameText() to work.
        InventoryView view = player.openAnvil(null, true);

        if (view instanceof AnvilView anvilView) {
            // Place paper in slot 0 — the client reads its display name as the
            // pre-filled text in the rename field.
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(initialText));
                paper.setItemMeta(meta);
            }
            anvilView.getTopInventory().setItem(0, paper);
            // Set cost to 0 so any player can confirm regardless of XP level.
            anvilView.setRepairCost(0);
        }
    }

    /**
     * Opens an anvil text-input for the given player.
     *
     * @param player      The player to open the anvil for.
     * @param plugin      The owning plugin (for listener registration).
     * @param initialText Text pre-filled in the rename field.
     * @param title       Kept for API compatibility; not used (real anvil title is default).
     * @param onConfirm   Called with the typed text when the player clicks the output slot.
     */
    public static void open(
        @NotNull Player player,
        @NotNull Plugin plugin,
        @NotNull String initialText,
        @NotNull String title,
        @Nullable Consumer<String> onConfirm
    ) {
        new AnvilInput(player, plugin, initialText, onConfirm);
    }

    // -------------------------------------------------------------------------
    // Event handlers
    // -------------------------------------------------------------------------

    /**
     * Keep repair cost at 0 so the output slot stays clickable regardless of
     * the player's XP level. Paper recalculates cost on every item change, so
     * we reset it here every time.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(@NotNull PrepareAnvilEvent event) {
        if (event.getViewers().stream().noneMatch(v -> v.getUniqueId().equals(playerUuid))) return;
        event.getView().setRepairCost(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!event.getWhoClicked().getUniqueId().equals(playerUuid)) return;
        if (event.getInventory().getType() != InventoryType.ANVIL) return;

        event.setCancelled(true);

        if (event.getRawSlot() == OUTPUT_SLOT) {
            String text = extractRenameText(event);

            // Clear slots before closing so items don't return to player inventory.
            event.getInventory().clear();

            unregister();
            event.getWhoClicked().closeInventory();

            if (onConfirm != null) {
                onConfirm.accept(text);
            }
        }
    }

    /**
     * Extracts the rename text from an anvil click event.
     *
     * <p>Primary path: {@link AnvilView#getRenameText()} — available on Paper 1.21+
     * with typed inventory views.
     *
     * <p>Fallback A: read the display name of the output item (slot 2) — Bukkit
     * copies the rename text to the result item's display name.
     *
     * <p>Fallback B: read the display name of the input item (slot 0) which we
     * placed ourselves as the pre-filled text.
     */
    @NotNull
    private String extractRenameText(@NotNull InventoryClickEvent event) {
        // Primary: typed AnvilView (Paper 1.21+)
        if (event.getView() instanceof AnvilView anvilView) {
            String renamed = anvilView.getRenameText();
            if (renamed != null && !renamed.isEmpty()) return renamed;
        }

        // Fallback A: output slot item display name
        ItemStack output = event.getInventory().getItem(OUTPUT_SLOT);
        if (output != null && output.hasItemMeta()) {
            Component displayName = output.getItemMeta().displayName();
            if (displayName != null) {
                String text = PlainTextComponentSerializer.plainText().serialize(displayName);
                if (!text.isEmpty()) return text;
            }
        }

        // Fallback B: input slot item display name (what we pre-filled)
        ItemStack input = event.getInventory().getItem(0);
        if (input != null && input.hasItemMeta()) {
            Component displayName = input.getItemMeta().displayName();
            if (displayName != null) {
                return PlainTextComponentSerializer.plainText().serialize(displayName);
            }
        }

        return "";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (!event.getPlayer().getUniqueId().equals(playerUuid)) return;
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        // Clear in case player closed manually (pressed Escape) so paper doesn't drop.
        event.getInventory().clear();
        unregister();
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private void unregister() {
        if (!consumed) {
            consumed = true;
            HandlerList.unregisterAll(this);
        }
    }
}
