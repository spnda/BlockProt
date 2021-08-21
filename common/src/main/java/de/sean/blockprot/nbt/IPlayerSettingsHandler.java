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

package de.sean.blockprot.nbt;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * A simple handler to get a player's BlockProt settings.
 *
 * @since 0.2.3
 */
public abstract class IPlayerSettingsHandler<T, P> extends INBTHandler<T> {
    protected static final String LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place";

    protected static final String DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends";

    protected static final String PLAYER_SEARCH_HISTORY = "blockprot_player_search_history";

    /**
     * The player that this settings handler is getting values
     * for.
     *
     * @since 0.2.3
     */
    public final P player;

    protected IPlayerSettingsHandler(@NotNull final P player) {
        this.player = player;
    }

    /**
     * Check if the player wants their blocks to be locked when
     * placed.
     *
     * @return Returns true, if lock on place has not been set, otherwise
     * will return the player's setting.
     * @since 0.2.3
     */
    public abstract boolean getLockOnPlace();

    /**
     * Set the value of the lock on place setting. If true, the
     * player wants to lock any block right after placing it.
     *
     * @param value The boolean value to set it to.
     * @since 0.2.3
     */
    public abstract void setLockOnPlace(final boolean value);

    /**
     * Get the {@link List} of default friends for this player.
     *
     * @return A List of Player {@link UUID}s as {@link String}s
     * representing each friend.
     * @since 0.2.3
     */
    @NotNull
    public abstract List<String> getDefaultFriends();


    /**
     * Set a new list of default friends. These have to be UUID-based,
     * otherwise other callers using {@link #getDefaultFriends()} will
     * experience issues. This does not get checked.
     *
     * @param friends A list of UUIDs representing a list of friends.
     * @since 0.2.3
     */
    public abstract void setDefaultFriends(@NotNull final List<String> friends);

    /**
     * Gets the default friends as a list of {@code P}. Uses
     * {@link #getDefaultFriends} as a base.
     *
     * @return All default friends as a list of {@code P}.
     * @since 0.2.3
     */
    @NotNull
    public abstract List<P> getDefaultFriendsAsPlayers();
}
