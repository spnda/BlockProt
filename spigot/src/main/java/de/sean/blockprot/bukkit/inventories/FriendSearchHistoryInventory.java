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

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.nbt.FriendModifyAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Basic inventory showing the last 25 players that the player
 * searched for. This can be used to easily add a player that they
 * had just searched for again.
 */
public class FriendSearchHistoryInventory extends BlockProtInventory {
    private final int maxSkulls = getSize() - 2;

    @Override
    int getSize() {
        return InventoryConstants.tripleLine;
    }

    @Override
    @NotNull String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__SEARCH_HISTORY);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE -> closeAndOpen(player, new FriendManageInventory().fill(player));
            case PLAYER_HEAD, SKELETON_SKULL -> {
                int index = findItemIndex(item);
                if (index >= 0 && index < state.friendResultCache.size()) {
                    OfflinePlayer friend = state.friendResultCache.get(index);
                    modifyFriendsForAction(player, friend, FriendModifyAction.ADD_FRIEND);
                    closeAndOpen(player, new FriendManageInventory().fill(player));
                }
            }
            default -> closeAndOpen(player, null);
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    public Inventory fill(Player player) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
        final List<String> searchHistory = settingsHandler.getSearchHistory();

        state.friendResultCache.clear();
        final int max = Math.min(searchHistory.size(), maxSkulls);
        for (int i = 0; i < max; i++) {
            this.setItemStack(i, Material.SKELETON_SKULL, searchHistory.get(i));
            state.friendResultCache.add(Bukkit.getOfflinePlayer(UUID.fromString(searchHistory.get(i))));
        }

        setBackButton();

        Bukkit.getScheduler().runTaskAsynchronously(
            BlockProt.getInstance(),
            () -> {
                for (int i = 0; i < max; i++) {
                    this.setPlayerSkull(i, state.friendResultCache.get(i));
                }
            }
        );

        return this.inventory;
    }
}
