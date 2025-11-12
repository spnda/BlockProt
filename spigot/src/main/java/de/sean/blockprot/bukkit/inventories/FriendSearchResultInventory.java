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

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.nbt.FriendModifyAction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FriendSearchResultInventory extends BlockProtInventory {
    final ConcurrentLinkedQueue<Profile> resultQueue = new ConcurrentLinkedQueue<>();

    private final int maxResults = getSize() - 1;

    BukkitTask loadTask = null;
    BukkitTask updateTask = null;

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
                final var meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    final var id = meta.getOwningPlayer().getUniqueId();
                    modifyFriendsForAction(player, id, FriendModifyAction.ADD_FRIEND);
                    closeAndOpen(player, new FriendManageInventory().fill(player));

                    // Update the search history
                    PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
                    settingsHandler.addPlayerToSearchHistory(id);
                }
            }
            default -> closeAndOpen(player, null);
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        if (loadTask != null)
            loadTask.cancel();
        if (updateTask != null)
            updateTask.cancel();
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

        updateTask = Bukkit.getScheduler().runTaskTimer(BlockProt.getInstance(), new ResultUpdateTask(state), 0, 1);
        loadTask = Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), new AsyncResultLoadTask(state, player, searchQuery));

        for (int i = 0; i < maxResults; i++) {
            this.setItemStack(i, Material.SKELETON_SKULL, "Loading...");
        }
        setBackButton();
        return inventory;
    }

    /** This task is responsible for taking available results and inserting it into the inventory */
    private class ResultUpdateTask implements Runnable {
        InventoryState state;
        int playersIndex = 0;

        ResultUpdateTask(@NotNull InventoryState state) {
            this.state = state;
        }

        @Override
        public void run() {
            final var scheduler = Bukkit.getScheduler();
            if (!scheduler.isQueued(loadTask.getTaskId()) && !scheduler.isCurrentlyRunning(loadTask.getTaskId()) && resultQueue.isEmpty()) {
                if (playersIndex == 0) {
                    // If the task has stopped running and there are no results, clear the inventory
                    for (int i = 0; i < maxResults; i++) {
                        inventory.clear(i);
                    }
                }
                loadTask.cancel();
                updateTask.cancel();
            }

            Profile profile;
            while ((profile = resultQueue.poll()) != null && playersIndex < maxResults) {
                // Clear all the skeleton skulls named "Loading"
                if (playersIndex == 0) {
                    for (int i = 0; i < maxResults; i++) {
                        inventory.clear(i);
                    }
                }

                state.friendResultCache.add(profile.getUniqueId());

                setPlayerSkull(playersIndex, Bukkit.getServer().createPlayerProfile(profile.getUniqueId(), profile.getName()));
                ++playersIndex;
            }

            // If we hit the maximum amount of results we can just tell the task to stop, and cancel ourselves.
            if (playersIndex == maxResults) {
                loadTask.cancel();
                updateTask.cancel();
            }
        }
    }

    /** This task asynchronously loads all possible players and filters them based on the search criteria.
     * It then adds every possible result to a queue that the {@link ResultUpdateTask} then consumes. */
    private class AsyncResultLoadTask implements Runnable {
        InventoryState state;
        Player player;
        String searchQuery;

        AsyncResultLoadTask(@NotNull InventoryState state, @NotNull Player player, @NotNull String searchQuery) {
            this.state = state;
            this.player = player;
            this.searchQuery = searchQuery;
        }

        @Override
        public void run() {
            double minimumSimilarity = BlockProt.getDefaultConfig().getFriendSearchSimilarityPercentage();
            final var offlinePlayers = Bukkit.getOfflinePlayers();

            final var stream = Arrays.stream(offlinePlayers)
                .map(OfflinePlayer::getUniqueId)
                // Other plugins/mods might use other UUID versions for NPCs or other players.
                .filter(uuid -> uuid.version() == 3 || uuid.version() == 4 || uuid.version() == 0);

            try {
                var filterStream = BlockProt.getProfileService().findAllByUuid(stream.toList()).stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getName() != null && !p.getUniqueId().equals(player.getUniqueId()))
                    .map(p -> new ImmutablePair<>(p, compareStrings(p.getName(), searchQuery)))
                    .filter(p -> p.right >= minimumSimilarity)
                    .sorted((a, b) -> b.right.compareTo(a.right))
                    .map(p -> p.left);

                if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.getBlock() != null) {
                    filterStream = filterStream
                            .filter(f -> PluginIntegration.filterFriendByUuidForAll(f.getUniqueId(), player, state.getBlock()));
                }

                filterStream.limit(maxResults).forEach(resultQueue::add);
            } catch (Exception e) {
                BlockProt.getInstance().getLogger().warning("Failed to search and filter players during friend search: " + e.getMessage());
            }
        }
    }
}
