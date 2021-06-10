/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.sean.blockprot.bukkit.nbt;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerSettingsHandler extends LockHandler<NBTCompound> {
    public final Player player;

    public PlayerSettingsHandler(@NotNull final Player player) {
        super();
        this.player = player;

        this.container = new NBTEntity(player).getPersistentDataContainer();
    }

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
        if (!container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)) return Collections.emptyList();
        else {
            return LockUtil
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
        }
    }

    @NotNull
    public List<OfflinePlayer> getDefaultFriendsAsPlayers() {
        if (!container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)) return Collections.emptyList();
        else {
            return LockUtil
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE))
                .stream()
                .map(s -> Bukkit.getOfflinePlayer(UUID.fromString(s)))
                .collect(Collectors.toList());
        }
    }

    public void setDefaultFriends(@NotNull final List<String> friends) {
        container.setString(DEFAULT_FRIENDS_ATTRIBUTE, friends.toString());
    }
}
