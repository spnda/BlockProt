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
public final class PlayerSettingsHandler extends NBTHandler<NBTCompound> {
    static final String LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place";

    static final String DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends";

    static final String PLAYER_SEARCH_HISTORY = "blockprot_player_search_history";

    private static final int MAX_HISTORY_SIZE = InventoryConstants.tripleLine - 2;

    /**
     * The player that this settings handler is getting values
     * for.
     *
     * @since 0.2.3
     */
    public final Player player;

    /**
     * Create a new settings handler.
     *
     * @param player The player to get the settings for.
     * @since 0.2.3
     */
    public PlayerSettingsHandler(@NotNull final Player player) {
        super();
        this.player = player;

        this.container = new NBTEntity(player).getPersistentDataContainer();
    }

    /**
     * Check if the player wants their blocks to be locked when
     * placed.
     *
     * @return Will return the default setting from the config, or the
     * value the player has set it to.
     * @since 0.2.3
     */
    public boolean getLockOnPlace() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(LOCK_ON_PLACE_ATTRIBUTE))
            return BlockProt.getDefaultConfig().lockOnPlaceByDefault();
        return container.getBoolean(LOCK_ON_PLACE_ATTRIBUTE);
    }

    /**
     * Set the value of the lock on place setting. If true, the
     * player wants to lock any block right after placing it.
     *
     * @param lockOnPlace The boolean value to set it to.
     * @since 0.2.3
     */
    public void setLockOnPlace(final boolean lockOnPlace) {
        container.setBoolean(LOCK_ON_PLACE_ATTRIBUTE, lockOnPlace);
    }

    /**
     * Get the {@link List} of default friends for this player.
     *
     * @return A List of Player {@link UUID}s as {@link String}s
     * representing each friend.
     * @since 0.2.3
     */
    @NotNull
    public List<String> getDefaultFriends() {
        if (!container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)) return new ArrayList<>();
        else {
            return BlockProtUtil
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
        }
    }

    /**
     * Set a new list of default friends. These have to be UUID-based,
     * otherwise other callers using {@link #getDefaultFriends()} will
     * experience issues. This does not get checked.
     *
     * @param friends A list of UUIDs representing a list of friends.
     * @since 0.2.3
     */
    public void setDefaultFriends(@NotNull final List<String> friends) {
        container.setString(DEFAULT_FRIENDS_ATTRIBUTE, friends.toString());
    }

    /**
     * Gets the default friends as a list of {@link OfflinePlayer}. Uses
     * {@link #getDefaultFriends} as a base.
     *
     * @return All default friends as a list of {@link OfflinePlayer}.
     * @since 0.2.3
     */
    @NotNull
    public List<OfflinePlayer> getDefaultFriendsAsPlayers() {
        ArrayList<String> friends = (ArrayList<String>) getDefaultFriends();
        return friends
            .stream()
            .map(s -> Bukkit.getOfflinePlayer(UUID.fromString(s)))
            .collect(Collectors.toList());
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
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
        if (!(handler instanceof PlayerSettingsHandler)) return;
        final PlayerSettingsHandler playerSettingsHandler = (PlayerSettingsHandler) handler;
        this.setLockOnPlace(playerSettingsHandler.getLockOnPlace());
        this.container.setString(DEFAULT_FRIENDS_ATTRIBUTE,
            playerSettingsHandler.container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
    }
}
