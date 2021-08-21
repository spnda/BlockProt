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

package de.sean.blockprot.fabric.nbt;

import de.sean.blockprot.fabric.ext.PlayerEntityNbtExtension;
import de.sean.blockprot.nbt.INBTHandler;
import de.sean.blockprot.nbt.IPlayerSettingsHandler;
import de.sean.blockprot.util.BlockProtUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple handler to get a player's BlockProt settings.
 *
 * @since 0.2.3
 */
public final class PlayerSettingsHandler extends IPlayerSettingsHandler<PlayerEntityNbtExtension, PlayerEntity> {
    /**
     * Create a new settings handler.
     *
     * @param player The player to get the settings for.
     * @since 0.2.3
     */
    public PlayerSettingsHandler(PlayerEntity player) {
        super(player);
        this.container = (PlayerEntityNbtExtension) player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLockOnPlace() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.contains(LOCK_ON_PLACE_ATTRIBUTE)) return true;
        return container.getBoolean(LOCK_ON_PLACE_ATTRIBUTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLockOnPlace(boolean value) {
        container.putBoolean(LOCK_ON_PLACE_ATTRIBUTE, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<String> getDefaultFriends() {
        if (!container.contains(DEFAULT_FRIENDS_ATTRIBUTE)) return new ArrayList<>();
        else {
            return BlockProtUtil
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultFriends(@NotNull List<String> friends) {
        container.putString(DEFAULT_FRIENDS_ATTRIBUTE, friends.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public @NotNull List<PlayerEntity> getDefaultFriendsAsPlayers() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getName() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeHandler(@NotNull INBTHandler<?> handler) {
        if (!(handler instanceof final PlayerSettingsHandler otherHandler)) return;
        this.setLockOnPlace(otherHandler.getLockOnPlace());
        this.container.putString(DEFAULT_FRIENDS_ATTRIBUTE,
            otherHandler.container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
    }
}
