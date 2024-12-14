/*
 * Copyright (C) 2021 - 2024 spnda
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

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base NBT Handler.
 *
 * @param <T> The type of the NBT container. {@link NBTCompound}
 *            is the base of all containers and works with all of them,
 *            however there are some special conditions and functions
 *            for specific containers.
 * @since 0.3.0
 */
public abstract class NBTHandler<T extends ReadWriteNBT> {
    @Deprecated
    public static final String PERMISSION_LOCK = "blockprot.lock";

    @Deprecated
    public static final String PERMISSION_INFO = "blockprot.info";

    @Deprecated
    public static final String PERMISSION_ADMIN = "blockprot.admin";

    @Deprecated
    public static final String PERMISSION_BYPASS = "blockprot.bypass";

    /**
     * The NBT container for this handler.
     *
     * @since 0.3.0
     */
    protected T container;

    @Nullable private final String name;

    /**
     * Create a new base NBTHandler.
     *
     * @since 0.3.0
     */
    protected NBTHandler(@Nullable final String name) {
        this.name = name;
    }

    /**
     * Get the name of the nbt container.
     *
     * @return The name, or empty if none exists.
     * @since 0.4.4
     */
    @NotNull
    public String getName() {
        return name == null ? "" : name;
    }

    /**
     * Copies all values of the other handler to this handler.
     *
     * @param handler The handler to copy values from.
     * @since 0.3.2
     */
    public void mergeHandler(@NotNull final NBTHandler<?> handler) {}

    /**
     * Gets a copy of this NBT inside a new {@link NBTContainer}.
     * @since 1.0.0
     */
    @NotNull
    public ReadWriteNBT getNbtCopy() {
        final var copy = NBT.createNBTObject();
        copy.mergeCompound(this.container);
        return copy;
    }

    /**
     * Pastes given NBT into this container, potentially
     * overriding everything.
     * 
     * @param container The NBT to paste.
     * @since 1.0.0
     */
    public void pasteNbt(@NotNull ReadWriteNBT container) {
        this.container.mergeCompound(container);
    }
}
