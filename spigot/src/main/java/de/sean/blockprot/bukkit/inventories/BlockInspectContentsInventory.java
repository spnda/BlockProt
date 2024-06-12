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

package de.sean.blockprot.bukkit.inventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Inventory to mirror the contents of another inventory. Works for any inventory type, including
 * furnaces, hoppers, etc.
 * @since 1.1.7
 */
public class BlockInspectContentsInventory extends BlockProtInventory {
    private final InventoryHolder sourceInventory;

    public BlockInspectContentsInventory(@NotNull final Player player) {
        super(false);
        final var state = InventoryState.get(player.getUniqueId());
        if (state == null)
            throw new RuntimeException("Attempting to create a inventory with no inventory state available.");

        if (!(state.getBlock().getState() instanceof InventoryHolder))
            throw new RuntimeException("Attempting to create a contents inventory for a block without an inventory.");

        this.sourceInventory = (InventoryHolder) state.getBlock().getState();
        var type = sourceInventory.getInventory().getType();
        if (type == InventoryType.CHEST) {
            this.inventory = createInventory();
        } else {
            this.inventory = Bukkit.createInventory(this, this.sourceInventory.getInventory().getType());
        }
    }

    @Override
    int getSize() {
        return sourceInventory.getInventory().getSize();
    }

    @Override
    @Nullable final String getTranslatedInventoryName() {
        return null;
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        // This inventory is only to inspect the contents, not for taking items out.
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    public Inventory fill() {
        this.inventory.setContents(sourceInventory.getInventory().getContents());
        return inventory;
    }
}
