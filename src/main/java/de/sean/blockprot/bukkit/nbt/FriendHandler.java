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

import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class FriendHandler extends NBTHandler<NBTCompound> {
    static final String ACCESS_FLAGS_ATTRIBUTE = "blockprot_access_flags";

    public FriendHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
    }

    @NotNull
    public String getName() {
        String name = container.getName();
        return name == null ? "" : name;
    }

    /**
     * Read the access flags of this block.
     */
    @NotNull
    public EnumSet<BlockAccessFlag> getAccessFlags() {
        if (!container.hasKey(ACCESS_FLAGS_ATTRIBUTE)) return EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE);
        else return BlockAccessFlag.parseFlags(container.getInteger(ACCESS_FLAGS_ATTRIBUTE));
    }

    /**
     * Sets the access flags for this block. ORs all flags together to one integer, then
     * writes all of them to ACCESS_FLAGS_ATTRIBUTE.
     */
    public void setAccessFlags(@NotNull final EnumSet<BlockAccessFlag> flags) {
        container.setInteger(ACCESS_FLAGS_ATTRIBUTE, flags.stream().mapToInt(BlockAccessFlag::getFlag).sum());
    }

    public boolean canRead() {
        return getAccessFlags().contains(BlockAccessFlag.READ);
    }

    public boolean canWrite() {
        return getAccessFlags().contains(BlockAccessFlag.WRITE);
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
    }
}
