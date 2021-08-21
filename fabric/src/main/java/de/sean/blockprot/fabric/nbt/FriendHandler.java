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

import de.sean.blockprot.nbt.IFriendHandler;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class FriendHandler extends IFriendHandler<NbtCompound, BlockAccessFlag> {
    FriendHandler(@NotNull final String name, @NotNull final NbtCompound compound) {
        super(name);
        this.container = compound;
    }

    @Override
    protected int getAccessFlagsBitset() {
        if (!container.contains(ACCESS_FLAGS_ATTRIBUTE)) return BlockAccessFlag.READ.getFlag() | BlockAccessFlag.WRITE.getFlag();
        else return container.getInt(ACCESS_FLAGS_ATTRIBUTE);
    }

    @Override
    protected void setAccessFlagsBitset(final int bitset) {
        container.putInt(ACCESS_FLAGS_ATTRIBUTE, bitset);
    }

    @Override
    public @NotNull EnumSet<BlockAccessFlag> getAccessFlags() {
        // We don't just use #getAccessFlagsBitset, as it would have a minor overhead due to
        // using the BlockAccessFlag#parseFlags method.
        if (!container.contains(ACCESS_FLAGS_ATTRIBUTE)) return EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE);
        else return BlockAccessFlag.parseFlags(container.getInt(ACCESS_FLAGS_ATTRIBUTE));
    }

    @Override
    public void setAccessFlags(@NotNull EnumSet<BlockAccessFlag> flags) {
        container.putInt(ACCESS_FLAGS_ATTRIBUTE, flags.stream().mapToInt(BlockAccessFlag::getFlag).sum());
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
