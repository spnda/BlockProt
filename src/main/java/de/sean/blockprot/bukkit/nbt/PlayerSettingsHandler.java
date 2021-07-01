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

import de.sean.blockprot.config.BlockProtConfig;
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

public class PlayerSettingsHandler extends NBTHandler<NBTCompound> {
    static final String LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place";
    static final String DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends";

    public final Player player;

    public PlayerSettingsHandler(@NotNull final Player player) {
        super();
        this.player = player;

        this.container = new NBTEntity(player).getPersistentDataContainer();
    }

    /**
     * Check if the given [player] wants their blocks to be locked when
     * placed.
     */
    public boolean getLockOnPlace() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(LOCK_ON_PLACE_ATTRIBUTE)) return true;
        return container.getBoolean(LOCK_ON_PLACE_ATTRIBUTE);
    }

    public void setLockOnPlace(final boolean lockOnPlace) {
        container.setBoolean(LOCK_ON_PLACE_ATTRIBUTE, lockOnPlace);
    }

    @NotNull
    public List<String> getDefaultFriends() {
        if (!container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)) return new ArrayList<>();
        else {
            return BlockProtConfig
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
        }
    }

    @NotNull
    public List<OfflinePlayer> getDefaultFriendsAsPlayers() {
        if (!container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)) return new ArrayList<>();
        else {
            return BlockProtConfig
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE))
                .stream()
                .map(s -> Bukkit.getOfflinePlayer(UUID.fromString(s)))
                .collect(Collectors.toList());
        }
    }

    public void setDefaultFriends(@NotNull final List<String> friends) {
        container.setString(DEFAULT_FRIENDS_ATTRIBUTE, friends.toString());
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
    }
}
