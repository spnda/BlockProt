/*
 * Copyright (C) 2021 - 2023 spnda
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
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The backbone of any handler that supports adding a list of friends/players.
 * 
 * @since 1.0.0
 */
public abstract class FriendSupportingHandler<T extends NBTCompound> extends NBTHandler<T> {
    private final @NotNull String friendNbtKey;

    public static final String zeroedUuid = new UUID(0, 0).toString();
    
    public FriendSupportingHandler(@NotNull String friendNbtKey) {
        this.friendNbtKey = friendNbtKey;
    }

    private NBTCompound compound() {
        return container.getOrCreateCompound(friendNbtKey);
    }

    /**
     * A function called before friends are read from this NBT compound.
     * Useful for remapping old data to this new structure.
     */
    protected void preFriendReadCallback() {

    }

    /**
     * Gets a {@link Stream} of {@link FriendHandler} for this block.
     *
     * @return A stream of friend handlers for all NBT compounds under
     * the friend key.
     */
    public Stream<FriendHandler> getFriendsStream() {
        preFriendReadCallback();
        if (!this.container.hasTag(friendNbtKey)) return Stream.empty();
        if (BlockProt.getDefaultConfig().isFriendFunctionalityDisabled()) return Stream.empty();

        final NBTCompound compound = this.container.getOrCreateCompound(friendNbtKey);
        return compound
            .getKeys()
            .stream()
            .map((k) -> new FriendHandler(compound.getCompound(k)));
    }

    /**
     * Gets a {@link List} of {@link FriendHandler} for this block.
     */
    public List<FriendHandler> getFriends() {
        return this.getFriendsStream().collect(Collectors.toList());
    }

    /**
     * Gets friends as a list of {@link OfflinePlayer}. Note that this call
     * could be expensive and block the current thread due to web-based API calls.
     *
     * Note that this will return every player on the server if everyone was added
     * to the block.
     */
    public List<OfflinePlayer> getFriendsAsPlayers() {
        return this.getFriendsStream()
            .flatMap(f -> {
                if (f.getName().equals(zeroedUuid)) {
                    return Arrays.stream(Bukkit.getOfflinePlayers());
                } else {
                    return Stream.of(f);
                }
            })
            .map(f -> {
                if (f instanceof FriendHandler fh) {
                    return Bukkit.getOfflinePlayer(UUID.fromString(fh.getName()));
                } else {
                    return (OfflinePlayer) f;
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Set a new list of FriendHandler for the friends list.
     */
    public void setFriends(@NotNull final List<FriendHandler> friends) {
        container.removeKey(friendNbtKey);
        friends.forEach(this::addFriend);
    }

    /**
     * Filters the results of {@link #getFriends()} for any entry whose
     * UUID qualifies for {@link String#equals(Object)} with given {@code id}.
     *
     * If the given {@code id} is not a friend of this block, this might return
     * the {@link FriendHandler} which represents all players.
     *
     * @param id The UUID to check for.
     * @return The first {@link FriendHandler} found, or none.
     */
    @NotNull
    public Optional<FriendHandler> getFriend(@NotNull final String id) {
        return getFriendsStream()
            .filter(f -> f.getName().equals(id) || f.getName().equals(zeroedUuid))
            // This is a weird Comparator, but it essentially just guarantees that the entry where
            // getName().equals(zeroedUuid) is at the end of the stream.
            .min((a, b) -> a.getName().equals(zeroedUuid) ? 1 : -1);
    }

    /**
     * Adds a new friend to the NBT.
     */
    public void addFriend(@NotNull final String friend) {
        compound().addCompound(friend).setString("id", friend);
    }

    /**
     * Adds a new {@link FriendHandler} to this NBT data.
     */
    public void addFriend(@NotNull final FriendHandler friend) {
        compound().addCompound(friend.getName()).mergeCompound(friend.container);
    }

    /**
     * Add "everyone" as a friend to this handler.
     * @since 1.1.0
     */
    public void addEveryoneAsFriend() {
        addFriend(zeroedUuid);
    }

    /**
     * Removes a friend from the NBT.
     */
    public void removeFriend(@NotNull final String friend) {
        compound().removeKey(friend);
    }

    /**
     * Checks whether this blocks friends contain given {@code friendUuid}.
     * @see #containsFriend(Stream, String) 
     */
    public boolean containsFriend(@NotNull final String friendUuid) {
        return containsFriend(getFriendsStream(), friendUuid);
    }

    /**
     * Checks whether {@code friends} contains {@code friend}.
     *
     * @param friends A list of all friends we want to filter.
     * @param friendUuid The UUID of a player we want to check for.
     * @return True, if the list does contain that friend.
     */
    public boolean containsFriend(@NotNull final Stream<FriendHandler> friends, @NotNull final String friendUuid) {
        return friends
            .anyMatch((f) -> f.getName().equals(friendUuid));
    }
}
