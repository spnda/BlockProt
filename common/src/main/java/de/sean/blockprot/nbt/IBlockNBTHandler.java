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
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A block handler to get values and settings from a single lockable
 * block.
 *
 * @since 0.2.3
 */
public abstract class IBlockNBTHandler<T, K, F extends IFriendHandler<?, ?>> extends INBTHandler<T> {
    protected static final boolean DEFAULT_REDSTONE = true;

    protected static final String OWNER_ATTRIBUTE = "splugin_owner";

    protected static final String OLD_LOCK_ATTRIBUTE = "splugin_lock";

    protected static final String LOCK_ATTRIBUTE = "blockprot_friends";

    protected static final String REDSTONE_ATTRIBUTE = "splugin_lock_redstone";

    /**
     * Reads the current owner from the NBT container.
     *
     * @return The owner as a UUID-String read from the container, or an empty String.
     * @since 0.2.3
     */
    @NotNull
    public abstract String getOwner();

    /**
     * Set the current owner of this block.
     *
     * @param owner The new owner for this block. Should
     *              be a valid UUID.
     * @since 0.2.3
     */
    public abstract void setOwner(@NotNull final String owner);

    /**
     * Gets a {@link Stream} of {@link IFriendHandler} for this block.
     *
     * @return A stream of friend handlers for all NBT compounds under
     * the friend key.
     * @since 0.3.0
     */
    @NotNull
    public abstract Stream<F> getFriendsStream();

    /**
     * Gets a {@link List} of friends for this block.
     *
     * @return A list of {@link IFriendHandler} to read
     * additional data for each friend.
     * @since 0.3.0
     */
    @NotNull
    public List<F> getFriends() {
        return getFriendsStream().collect(Collectors.toList());
    }

    /**
     * Set a new list of FriendHandler for the friends list.
     *
     * @param access The new list of friends to use.
     * @since 0.3.0
     */
    public abstract void setFriends(@NotNull final List<F> access);

    /**
     * Filters the results of {@link #getFriends()} for any entry which
     * id qualifies for {@link String#equals(Object)}.
     *
     * @param id The String ID to check for. Usually a UUID as a String as {@link UUID#toString()}.
     * @return The first {@link IFriendHandler} found, or none.
     * @since 0.3.0
     */
    @NotNull
    public Optional<F> getFriend(@NotNull final String id) {
        return getFriendsStream()
            .filter((f) -> f.getName().equals(id))
            .findFirst();
    }

    /**
     * Adds a new friend to the NBT.
     *
     * @param friend The friend to add.
     * @since 0.3.0
     */
    public abstract void addFriend(@NotNull final String friend);

    /**
     * Removes a friend from the NBT.
     *
     * @param friend The friend to remove.
     * @since 0.3.0
     */
    public abstract void removeFriend(@NotNull final String friend);

    /**
     * If true, redstone should be allowed for this block and should not be blocked.
     * If redstone has not been set for this block yet, the default value is true
     *
     * @return Whether redstone should be allowed or not.
     * @since 0.2.3
     */
    public abstract boolean getRedstone();

    /**
     * Set the new value for redstone. See {@link #getRedstone()} for more
     * details on the values.
     *
     * @param redstone The boolean value to set.
     * @since 0.2.3
     */
    public abstract void setRedstone(final boolean redstone);

    /**
     * This applies any changes to this container to a possible other
     * half. For example doors consist from two blocks, as do double
     * chests. Without this call, all methods will modify only the local,
     * current block.
     * <p>
     * This method is specifically not called on each modification of NBT,
     * as this would be a massive, unnecessary performance penalty.
     *
     * @since 0.4.6
     */
    public abstract void applyToOtherContainer();

    /**
     * Locks this block for given {@code player} as the owner.
     *
     * @param player The player to set as an owner.
     * @return A {@code L} whether or not the block was successfully locked,
     * else there might have been issues with permissions.
     * @since 0.4.6
     */
    @NotNull
    public abstract LockReturnValue lockBlock(@NotNull final K player);

    /**
     * Locks redstone for this block.
     *
     * @param player The player requesting this command, should be the owner.
     * @param value  The value we want to set it to. If null, we just flip
     *               the current value.
     * @return A {@code L} whether or not the redstone was switched
     * successfully.
     * @since 0.4.6
     */
    @NotNull
    public abstract LockReturnValue lockRedstoneForBlock(@NotNull final String player, @Nullable final Boolean value);

    /**
     * Whether or not this block is protected. This is evaluated by checking
     * if an owner exists and if any friends have been added to the block.
     *
     * @return True, if this block is not protected and there is no owner.
     * @since 0.2.3
     */
    public boolean isNotProtected() {
        return getOwner().isEmpty() && getFriends().isEmpty();
    }

    /**
     * @return True, if this block is protected.
     * @see #isNotProtected()
     * @since 0.2.3
     */
    public boolean isProtected() {
        return !isNotProtected();
    }

    /**
     * Checks whether or not given {@code player} is the owner of this block.
     *
     * @param player A String representing a players UUID.
     * @return Whether or not {@code player} is the owner of this block.
     * @since 0.2.3
     */
    public boolean isOwner(@NotNull final String player) {
        return getOwner().equals(player);
    }

    /**
     * Checks whether or not given {@code player} can access this block.
     *
     * @param player The player to check for.
     * @return True, if {@code player} can access this block.
     * @since 0.2.3
     */
    public boolean canAccess(@NotNull final String player) {
        Optional<F> friend = getFriend(player);
        return !isProtected() || (getOwner().equals(player) || (friend.isPresent() && friend.get().canRead()));
    }

    /**
     * Checks whether or not {@code friends} contains {@code friend}.
     *
     * @param friends A list of all friends we want to filter.
     * @param friend  The UUID of a player we want to check for.
     * @return True, if the list does contain that friend.
     * @since 0.3.0
     */
    protected boolean containsFriend(@NotNull final List<F> friends, @NotNull final String friend) {
        return friends
            .stream()
            .anyMatch((f) -> f.getName().equals(friend));
    }

    /**
     * Clears all values from this block and resets it to the
     * defaults.
     *
     * @since 0.3.2
     */
    public void clear() {
        this.setOwner("");
        this.setFriends(Collections.emptyList());
        this.setRedstone(DEFAULT_REDSTONE);
    }

    /**
     * Modifies the friends of this block for given {@code action}.
     *
     * @param player The player requesting this command, should be the owner.
     * @param friend The friend do to {@code action} with.
     * @param action The action we should perform with {@code friend} on this block.
     * @return A {@code L} whether or not the friends were modified
     * successfully.
     * @since 0.4.6
     */
    @NotNull
    public abstract LockReturnValue modifyFriends(@NotNull final String player, @NotNull final String friend, @NotNull final FriendModifyAction action);

    /**
     * Merges this handler with another {@link INBTHandler}.
     *
     * @param handler The handler to merge with. If {@code handler} is not an instance
     *                of {@link IBlockNBTHandler}, this will do nothing.
     * @since 0.3.2
     */
    @Override
    public void mergeHandler(@NotNull INBTHandler<?> handler) {
        if (!(handler instanceof IBlockNBTHandler)) return;
        final IBlockNBTHandler<?, ?, F> blockNBTHandler = (IBlockNBTHandler<?, ?, F>) handler;
        this.setOwner(blockNBTHandler.getOwner());
        this.setFriends(blockNBTHandler.getFriends());
        this.setRedstone(blockNBTHandler.getRedstone());
    }
}
