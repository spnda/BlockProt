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

import de.sean.blockprot.nbt.IFriendHandler;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * The friend handler used by {@link BlockNBTHandler} to handle
 * each of the {@link NBTCompound} used in the "friends" sub-tag of each block.
 * A single {@link FriendHandler} itself only handles a *single friend*
 * in the list of friends.
 *
 * @since 0.3.0
 */
public final class FriendHandler extends IFriendHandler<NBTCompound, BlockAccessFlag> {
    /**
     * @param compound The NBT compound used.
     * @since 0.3.0
     */
    public FriendHandler(@NotNull final NBTCompound compound) {
        super(compound.getName());
        this.container = compound;
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.3.0
     */
    @Override
    @NotNull
    public String getName() {
        String name = container.getName();
        return name == null ? "" : name;
    }

    /**
     * Read the access flags of this block as a bitset.
     *
     * @return A bitset of all access flags of this block.
     * @see #getAccessFlags()
     * @since 0.4.7
     */
    protected int getAccessFlagsBitset() {
        if (!container.hasKey(ACCESS_FLAGS_ATTRIBUTE)) return BlockAccessFlag.READ.getFlag() | BlockAccessFlag.WRITE.getFlag();
        else return container.getInteger(ACCESS_FLAGS_ATTRIBUTE);
    }

    /**
     * Sets the access flag bitset for this block.
     *
     * @param flagsBitset The new bitset.
     * @since 0.4.7
     */
    protected void setAccessFlagsBitset(final int flagsBitset) {
        container.setInteger(ACCESS_FLAGS_ATTRIBUTE, flagsBitset);
    }

    /**
     * Read the access flags of this block.
     *
     * @return A {@link EnumSet} of all flags for this block.
     * @since 0.3.0
     */
    @NotNull
    public EnumSet<BlockAccessFlag> getAccessFlags() {
        // We don't just use #getAccessFlagsBitset, as it would have a minor overhead due to
        // using the BlockAccessFlag#parseFlags method.
        if (!container.hasKey(ACCESS_FLAGS_ATTRIBUTE)) return EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE);
        else return BlockAccessFlag.parseFlags(container.getInteger(ACCESS_FLAGS_ATTRIBUTE));
    }

    /**
     * Sets the access flags for this block. ORs all flags together to one integer, then
     * writes all of them to ACCESS_FLAGS_ATTRIBUTE.
     *
     * @param flags The new flags to use. These get converted to integers.
     * @since 0.3.0
     */
    public void setAccessFlags(@NotNull final EnumSet<BlockAccessFlag> flags) {
        container.setInteger(ACCESS_FLAGS_ATTRIBUTE, flags.stream().mapToInt(BlockAccessFlag::getFlag).sum());
    }

    @Override
    public boolean canRead() {
        return getAccessFlags().contains(BlockAccessFlag.READ);
    }

    @Override
    public boolean canWrite() {
        return getAccessFlags().contains(BlockAccessFlag.WRITE);
    }
}
