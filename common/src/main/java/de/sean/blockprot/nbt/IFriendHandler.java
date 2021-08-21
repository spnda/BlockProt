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

import java.util.EnumSet;

/**
 * The friend handler used by {@link IBlockNBTHandler} to handle
 * each of the nbt compounds used in the "friends" sub-tag of each block.
 * A single {@link IFriendHandler} itself only handles a *single friend*
 * in the list of friends.
 *
 * @since 0.3.0
 */
public abstract class IFriendHandler<T, B extends Enum<B>> extends INBTHandler<T> {
    protected static final String ACCESS_FLAGS_ATTRIBUTE = "blockprot_access_flags";

    private final String name;

    protected IFriendHandler(@NotNull final String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Read the access flags of this block as a bitset.
     *
     * @return A bitset of all access flags of this block.
     * @see #getAccessFlags()
     * @since 0.4.7
     */
    protected abstract int getAccessFlagsBitset();

    /**
     * Sets the access flag bitset for this block.
     *
     * @param bitset The new bitset.
     * @since 0.4.7
     */
    protected abstract void setAccessFlagsBitset(final int bitset);

    /**
     * Read the access flags of this block.
     *
     * @return A {@link EnumSet} of all flags for this block.
     * @since 0.3.0
     */
    @NotNull
    public abstract EnumSet<B> getAccessFlags();

    /**
     * Sets the access flags for this block. ORs all flags together to one integer, then
     * writes all of them to ACCESS_FLAGS_ATTRIBUTE.
     *
     * @param flags The new flags to use. These get converted to integers.
     * @since 0.3.0
     */
    public abstract void setAccessFlags(@NotNull final EnumSet<B> flags);

    /**
     * Checks if this player can read the contents of the parents
     * block.
     *
     * @return True, if the player is allowed to see the container's
     * contents.
     * @since 0.3.0
     */
    public abstract boolean canRead();

    /**
     * Checks if this player can write the contents of the parents
     * block. This means that the player should be allowed to
     * take and add items at their will.
     *
     * @return True, if the player has write access to this block.
     * @since 0.3.0
     */
    public abstract boolean canWrite();

    /**
     * {@inheritDoc}
     * <p>
     * This only merges values if {@code handler} is an instance of {@link IFriendHandler},
     * and only merges the access flags.
     *
     * @since 0.4.7
     */
    @Override
    public void mergeHandler(@NotNull INBTHandler<?> handler) {
        if (handler instanceof IFriendHandler) {
            this.setAccessFlagsBitset(((IFriendHandler<?, ?>) handler).getAccessFlagsBitset());
        }
    }
}
