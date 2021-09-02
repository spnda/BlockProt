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
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.inventories.InventoryState.FriendSearchState;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendSupportingHandler;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FriendManageInventory extends BlockProtInventory {
    private int maxSkulls = getSize() - 5;

    @Override
    public int getSize() {
        return 9 * 6; // 6 Lines of inventory go brr
    }

    @NotNull
    @Override
    public String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__MANAGE);
    }

    /**
     * Exits this inventory depending on {@code state}'s {@link FriendSearchState} back to the {@link BlockLockInventory}
     * or the {@link UserSettingsInventory} respectively.
     *
     * @param player The player to open/close the inventory for.
     * @param state  The {@code player}'s state.
     */
    public void exitModifyInventory(@NotNull final Player player, @NotNull final InventoryState state) {
        Inventory newInventory;
        switch (state.friendSearchState) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) return;
                BlockNBTHandler handler = getNbtHandlerOrNull(state.getBlock());
                if (handler == null) {
                    newInventory = null;
                } else {
                    newInventory = new BlockLockInventory()
                            .fill(
                                player,
                                state.getBlock().getState().getType(),
                                handler);
                }
                break;
            }
            case DEFAULT_FRIEND_SEARCH:
                newInventory = new UserSettingsInventory().fill(player);
                break;
            default:
                return;
        }
        closeAndOpen(player, newInventory);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE: {
                // Exit the friend modify inventory and return to the base lock inventory.
                state.friendPage = 0;
                exitModifyInventory(player, state);
                break;
            }
            case CYAN_STAINED_GLASS_PANE: {
                if (state.friendPage >= 1) {
                    state.friendPage--;

                    closeAndOpen(player, fill(player));
                }
                break;
            }
            case BLUE_STAINED_GLASS_PANE: {
                ItemStack lastFriendInInventory = event.getInventory().getItem(maxSkulls - 1);
                if (lastFriendInInventory != null && lastFriendInInventory.getAmount() != 0) {
                    // There's an item in the last slot => The page is fully filled up, meaning
                    // we should go to the next page.
                    state.friendPage++;

                    closeAndOpen(player, fill(player));
                }
                break;
            }
            case SKELETON_SKULL:
            case PLAYER_HEAD: {
                // Get the clicked player head and open the detail inventory.
                int index = findItemIndex(item);
                if (index < 0 || index >= state.friendResultCache.size()) break;
                state.currentFriend = state.friendResultCache.get(index);
                final Inventory inv = new FriendDetailInventory().fill(player);
                closeAndOpen(player, inv);
                break;
            }
            case MAP: {
                FriendSearchInventory.openAnvilInventory(player);
                break;
            }
            case BOOK:
                closeAndOpen(player, new FriendSearchHistoryInventory().fill(player));
                break;
            default: {
                // Unexpected, exit the inventory.
                closeAndOpen(player, null);
                break;
            }
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    @Nullable
    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        final @Nullable FriendSupportingHandler<NBTCompound> handler =
            getFriendSupportingHandler(state.friendSearchState, player, state.getBlock());
        if (handler == null) return null;

        List<OfflinePlayer> players = handler.getFriendsAsPlayers();
        if (state.friendSearchState == FriendSearchState.FRIEND_SEARCH && state.getBlock() != null) {
            PluginIntegration.filterFriends(
                (ArrayList<OfflinePlayer>) players, player, state.getBlock());
        }

        // Fill the first page inventory with skeleton skulls.
        state.friendResultCache.clear();

        // We call fill() with the page buttons on this same holder. Clear the inventory too.
        this.inventory.clear();

        int pageOffset = maxSkulls * state.friendPage;
        for (int i = 0; i < Math.min(players.size() - pageOffset, maxSkulls); i++) {
            final OfflinePlayer curPlayer = players.get(pageOffset + i);
            if (curPlayer.getUniqueId().equals(player.getUniqueId())) continue;
            this.setItemStack(i, Material.SKELETON_SKULL, curPlayer.getName());
            state.friendResultCache.add(curPlayer);
        }

        // Only show the page buttons if there's more than 1 page.
        if (players.size() >= maxSkulls) {
            setItemStack(
                maxSkulls,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE);
            setItemStack(
                maxSkulls + 1,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE);
        }

        setItemStack(
            getSize() - 3,
            Material.BOOK,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH_HISTORY
        );
        setItemStack(
            getSize() - 2,
            Material.MAP,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH);
        setBackButton();

        Bukkit.getScheduler().runTaskAsynchronously(
            BlockProt.getInstance(),
            () -> {
                int i = 0;
                while (i < maxSkulls && i < state.friendResultCache.size()) {
                    this.setPlayerSkull(i, state.friendResultCache.get(i));
                    i++;
                }
            });

        return inventory;
    }
}

