/*
 * Copyright (C) 2021 - 2023 spnda
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

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.RedstoneSettingsHandler;
import de.sean.blockprot.nbt.LockReturnValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The settings inventory for all redstone relevant settings.
 * @since 0.4.13
 */
public class RedstoneSettingsInventory extends BlockProtInventory {
    private boolean currentProtection;
    private boolean hopperProtection;
    private boolean pistonProtection;
    private static final int SETTINGS_COUNT = 3;

    @Override
    int getSize() {
        return InventoryConstants.doubleLine;
    }

    @Override
    @NotNull String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__REDSTONE__SETTINGS);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        Player player = (Player) event.getWhoClicked();

        switch (item.getType()) {
            case REDSTONE -> {
                currentProtection = !currentProtection;
                inventory.setItem(0, toggleOption(item, null));
            }
            case HOPPER -> {
                hopperProtection = !hopperProtection;
                inventory.setItem(1, toggleOption(item, null));
            }
            case PISTON -> {
                pistonProtection = !pistonProtection;
                inventory.setItem(2, toggleOption(item, null));
            }
            case RED_STAINED_GLASS_PANE -> overrideAllSettings(false);
            case GREEN_STAINED_GLASS_PANE -> overrideAllSettings(true);
            default -> {
                BlockNBTHandler handler = getNbtHandlerOrNull(state.getBlock());
                closeAndOpen(
                    player,
                    handler == null
                        ? null
                        : new BlockLockInventory().fill(player, state.getBlock().getType(), handler)
                );
            }
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.getBlock() != null) {
            applyChanges(
                (Player) event.getPlayer(),
                (handler) -> {
                    // Apply new redstone values
                    RedstoneSettingsHandler redstoneHandler = handler.getRedstoneHandler();
                    redstoneHandler.setCurrentProtection(currentProtection);
                    redstoneHandler.setHopperProtection(hopperProtection);
                    redstoneHandler.setPistonProtection(pistonProtection);
                    return new LockReturnValue(true, null);
                },
                null
            );
        }
    }

    private void overrideAllSettings(final boolean value) {
        currentProtection = value;
        pistonProtection = value;
        hopperProtection = value;
        for (int i = 0; i < SETTINGS_COUNT; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null) {
                inventory.setItem(i, toggleEnchants(stack, value));
            }
        }
    }

    public Inventory fill(@NotNull Player player, @NotNull InventoryState state) {
        if (state.getBlock() == null) return inventory;
        BlockNBTHandler nbtHandler = getNbtHandlerOrNull(state.getBlock());
        if (nbtHandler == null) return inventory;

        // Make sure the player owns this
        if (!nbtHandler.isOwner(player.getUniqueId().toString())) return inventory;

        RedstoneSettingsHandler redstoneHandler = nbtHandler.getRedstoneHandler();
        currentProtection = redstoneHandler.getCurrentProtection();
        pistonProtection = redstoneHandler.getPistonProtection();
        hopperProtection = redstoneHandler.getHopperProtection();
        setEnchantedOptionItemStack(
            0,
            Material.REDSTONE,
            TranslationKey.INVENTORIES__REDSTONE__REDSTONE_PROTECTION,
            currentProtection
        );
        setEnchantedOptionItemStack(
            1,
            Material.HOPPER,
            TranslationKey.INVENTORIES__REDSTONE__HOPPER_PROTECTION,
            pistonProtection
        );
        setEnchantedOptionItemStack(
            2,
            Material.PISTON,
            TranslationKey.INVENTORIES__REDSTONE__PISTON_PROTECTION,
            hopperProtection
        );

        setItemStack(
            InventoryConstants.doubleLine - 3,
            Material.RED_STAINED_GLASS_PANE,
            TranslationKey.INVENTORIES__REDSTONE__DISABLE_ALL
        );
        setItemStack(
            InventoryConstants.doubleLine - 2,
            Material.GREEN_STAINED_GLASS_PANE,
            TranslationKey.INVENTORIES__REDSTONE__ENABLE_ALL
        );
        setBackButton();
        return inventory;
    }
}
