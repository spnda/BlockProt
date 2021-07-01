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

public abstract class NBTHandler<T extends NBTCompound> {
    public static final String PERMISSION_LOCK = "blockprot.lock";
    public static final String PERMISSION_INFO = "blockprot.info";
    public static final String PERMISSION_ADMIN = "blockprot.admin";
    public static final String PERMISSION_BYPASS = "blockprot.bypass";

    /**
     * The NBT container for this handler.
     */
    T container;

    protected NBTHandler() {
    }

    public abstract void mergeHandler(@NotNull final NBTHandler<?> handler);
}
