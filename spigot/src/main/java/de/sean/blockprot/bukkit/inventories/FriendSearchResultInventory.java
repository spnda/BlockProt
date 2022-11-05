/*
 * Copyright (C) 2021 - 2022 spnda
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
import de.sean.blockprot.bukkit.nbt.FriendSupportingHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.nbt.FriendModifyAction;
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
import java.util.Arrays;
import java.util.List;

public class FriendSearchResultInventory extends BlockProtInventory {
    @Override
    int getSize() {
        return InventoryConstants.tripleLine;
    }

    @NotNull
    @Override
    String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__RESULT);
    }

    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    // Optimized version of the Levenshtein Distance from here:
    // https://stackoverflow.com/a/13564498/9156308
    private int levenshteinDistance(@NotNull CharSequence a, @NotNull CharSequence b) {
        if (a.isEmpty())
            return b.length();

        if (b.isEmpty())
            return a.length();

        int[] mem = new int[b.length()];

        for (int i = 0; i < b.length(); ++i)
            mem[i] = i;

        for (int i = 1; i < a.length(); ++i) {
            int[] cur = new int[b.length()];
            cur[0] = i;

            for (int j = 1; j < b.length(); ++j) {
                int d1 = mem[j] + 1;
                int d2 = cur[j - 1] + 1;
                int d3 = mem[j - 1];
                if (a.charAt(i - 1) != b.charAt(j - 1))
                    d3 += 1;
                cur[j] = min(d1, d2, d3);
            }

            mem = cur;
        }

        return mem[b.length() - 1];
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE ->
                // As in the anvil inventory we cannot differentiate between
                // pressing Escape to go back, or closing it to go to the result
                // inventory, we won't return to the anvil inventory and instead
                // go right back to the FriendAddInventory.
                closeAndOpen(
                    player,
                    new FriendManageInventory().fill(player)
                );
            case PLAYER_HEAD, SKELETON_SKULL -> {
                int index = findItemIndex(item);
                if (index >= 0 && index < state.friendResultCache.size()) {
                    OfflinePlayer friend = state.friendResultCache.get(index);
                    modifyFriendsForAction(player, friend, FriendModifyAction.ADD_FRIEND);
                    closeAndOpen(player, new FriendManageInventory().fill(player));

                    // Update the search history
                    PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
                    settingsHandler.addPlayerToSearchHistory(friend);
                }
            }
            default -> closeAndOpen(player, null);
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    /**
     * Compare two strings by the levenshtein distance, returning a value between 0,
     * being totally unrelated strings, and 1, being identical or if both are empty.
     */
    private double compareStrings(String str1, String str2) {
        String longer = str1;
        String shorter = str2;
        if (str1.length() < str2.length()) {
            longer = str2;
            shorter = str1;
        }
        final int longerLength = longer.length();
        if (longerLength == 0) return 1.0; // They match 100% if both Strings are empty
        else return (longerLength - levenshteinDistance(longer, shorter)) / (double) longerLength;
    }

    @Nullable
    public Inventory fill(@NotNull Player player, String searchQuery) {
        InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        ArrayList<OfflinePlayer> potentialFriends = new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));

        // The already existing friends we want to add to.
        final @Nullable FriendSupportingHandler<NBTCompound> handler =
            getFriendSupportingHandler(state.friendSearchState, player, state.getBlock());
        if (handler == null) return null; // return null to indicate an issue.

        // We'll filter all doubled friends out of the list and add them to the current InventoryState.
        double minimumSimilarity = BlockProt.getDefaultConfig().getFriendSearchSimilarityPercentage();
        potentialFriends.removeIf(p -> {
            // Filter all the players by search criteria.
            // If the strings are similar by 30%, the strings are considered similar (imo) and should be added.
            // If they're less than 30% similar, we should still check if it possibly contains the search criteria
            // and still add that user.
            if (p.getName() == null || p.getUniqueId().equals(player.getUniqueId())) return true;
            else if (handler.containsFriend(p.getUniqueId().toString())) return true;
            else if (p.getName().contains(searchQuery)) return false;
            else return compareStrings(p.getName(), searchQuery) < minimumSimilarity;
        });

        state.friendResultCache.clear();
        if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.getBlock() != null) {
            // Allow integrations to additionally filter friends.
            var filtered = potentialFriends.stream()
                .filter(f -> PluginIntegration.filterFriendByUuidForAll(f.getUniqueId(), player, state.getBlock()))
                .toList();
            state.friendResultCache.addAll(filtered);
        } else {
            state.friendResultCache.addAll(potentialFriends);
        }


        // Finally, construct the inventory with all the potential friends.
        // To not delay when the inventory opens, we'll asynchronously get the items after
        // the inventory has been opened and later add them to the inventory. In the meantime,
        // we'll show the same amount of skeleton heads.
        final int maxPlayers = Math.min(potentialFriends.size(), InventoryConstants.tripleLine - 1);
        for (int i = 0; i < maxPlayers; i++) {
            this.setItemStack(i, Material.SKELETON_SKULL, potentialFriends.get(i).getName());
        }
        final List<OfflinePlayer> finalPotentialFriends = potentialFriends;
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), () -> {
            // Only show the 9 * 3 - 1 most relevant players. Don't show any extra.
            int playersIndex = 0;
            while (playersIndex < maxPlayers && playersIndex < finalPotentialFriends.size()) {
                // Only add to the inventory if this is not a friend (yet)
                setPlayerSkull(playersIndex, finalPotentialFriends.get(playersIndex));
                playersIndex++;
            }
        });
        setBackButton();
        return inventory;
    }
}
