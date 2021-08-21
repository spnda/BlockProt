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

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.inventories.InventoryConstants;
import de.sean.blockprot.nbt.INBTHandler;
import de.sean.blockprot.nbt.IPlayerSettingsHandler;
import de.sean.blockprot.util.BlockProtUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A simple handler to get a player's BlockProt settings.
 *
 * @since 0.2.3
 */
public final class PlayerSettingsHandler extends IPlayerSettingsHandler<NBTCompound, OfflinePlayer> {
    private static final int MAX_HISTORY_SIZE = InventoryConstants.tripleLine - 2;

    /**
     * Create a new settings handler.
     *
     * @param player The player to get the settings for.
     * @since 0.2.3
     */
    public PlayerSettingsHandler(@NotNull final Player player) {
        super(player);
        this.container = new NBTEntity(player).getPersistentDataContainer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLockOnPlace() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(LOCK_ON_PLACE_ATTRIBUTE))
            return BlockProt.getDefaultConfig().lockOnPlaceByDefault();
        return container.getBoolean(LOCK_ON_PLACE_ATTRIBUTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLockOnPlace(final boolean value) {
        container.setBoolean(LOCK_ON_PLACE_ATTRIBUTE, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getDefaultFriends() {
        if (!container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)) return new ArrayList<>();
        else {
            return BlockProtUtil
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultFriends(@NotNull final List<String> friends) {
        container.setString(DEFAULT_FRIENDS_ATTRIBUTE, friends.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<OfflinePlayer> getDefaultFriendsAsPlayers() {
        ArrayList<String> friends = (ArrayList<String>) getDefaultFriends();
        return friends
            .stream()
            .map(s -> Bukkit.getOfflinePlayer(UUID.fromString(s)))
            .collect(Collectors.toList());
    }

    @Override
    public @NotNull String getName() {
        String name = container.getName();
        return name == null ? "" : name;
    }

    /**
     * Get the current search history for this player.
     * 
     * @return A list of UUIDs for each player this player has
     * searched for.
     */
    public List<String> getSearchHistory() {
        if (!container.hasKey(PLAYER_SEARCH_HISTORY)) return new ArrayList<>();
        else {
            return BlockProtUtil
                .parseStringList(container.getString(PLAYER_SEARCH_HISTORY));
        }
    }

    /**
     * Add a player to the search history.
     * 
     * @param player The player to add.
     */
    public void addPlayerToSearchHistory(@NotNull final OfflinePlayer player) {
        this.addPlayerToSearchHistory(player.getUniqueId().toString());
    }

    /**
     * Add a player to the search history.
     * 
     * @param playerUuid The player UUID to add.
     */
    public void addPlayerToSearchHistory(@NotNull final String playerUuid) {
        List<String> history = getSearchHistory();
        if (!history.contains(playerUuid)) {
            // We want the list to not be bigger than MAX_HISTORY_SIZE,
            // therefore we remove the first entry if we would exceed that size.
            if (history.size() == MAX_HISTORY_SIZE) {
                history.remove(0);
            }
            history.add(playerUuid);
            container.setString(PLAYER_SEARCH_HISTORY, history.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeHandler(@NotNull INBTHandler<?> handler) {
        if (!(handler instanceof PlayerSettingsHandler)) return;
        final PlayerSettingsHandler playerSettingsHandler = (PlayerSettingsHandler) handler;
        this.setLockOnPlace(playerSettingsHandler.getLockOnPlace());
        this.container.setString(DEFAULT_FRIENDS_ATTRIBUTE,
            playerSettingsHandler.container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
    }
}
