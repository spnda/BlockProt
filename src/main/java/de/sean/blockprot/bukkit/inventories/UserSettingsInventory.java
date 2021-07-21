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
package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UserSettingsInventory extends BlockProtInventory {
    @Override
    int getSize() {
        return InventoryConstants.singleLine;
    }

    @NotNull
    @Override
    String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__USER_SETTINGS);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BARRIER:
                // Lock on place button, default value is true
                PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
                boolean lockOnPlace = !settingsHandler.getLockOnPlace();
                settingsHandler.setLockOnPlace(lockOnPlace);
                this.setItemStack(
                    0,
                    Material.BARRIER,
                    (lockOnPlace)
                        ? Translator.get(TranslationKey.INVENTORIES__LOCK_ON_PLACE__DEACTIVATE)
                        : Translator.get(TranslationKey.INVENTORIES__LOCK_ON_PLACE__ACTIVATE)
                );
                break;
            case PLAYER_HEAD:
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH;
                closeAndOpen(player, new FriendManageInventory().fill(player));
                break;
            default:
                closeAndOpen(player, null); // This also includes Material.BLACK_STAINED_GLASS_PANE
                break;
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
    }

    public Inventory fill(Player player) {
        PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
        boolean lockOnPlace = settingsHandler.getLockOnPlace();
        setItemStack(
            0,
            Material.BARRIER,
            (lockOnPlace)
                ? TranslationKey.INVENTORIES__LOCK_ON_PLACE__DEACTIVATE
                : TranslationKey.INVENTORIES__LOCK_ON_PLACE__ACTIVATE
        );
        setItemStack(
            1,
            Material.PLAYER_HEAD,
            TranslationKey.INVENTORIES__FRIENDS__MANAGE
        );
        setBackButton();
        return inventory;
    }
}
