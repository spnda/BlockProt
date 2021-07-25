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

/**
 * The base NBT Handler.
 *
 * @param <T> The type of the NBT container. A NBT compound should the
 *            base of all containers and works with all of them, however
 *            there are some special conditions and functions for specific
 *            containers.
 * @since 0.3.0
 */
public abstract class INBTHandler<T> {
    /**
     * The NBT container for this handler.
     *
     * @since 0.3.0
     */
    protected T container;

    /**
     * Create a new base NBTHandler.
     *
     * @since 0.3.0
     */
    protected INBTHandler() {
    }

    /**
     * Get a copy of the underlying NBT container.
     *
     * @return The NBT container.
     * @since 0.4.7
     */
    @NotNull
    public T getContainer() {
        return container;
    }

    /**
     * Get the name of the nbt container.
     *
     * @return The name, or empty if none exists.
     * @since 0.4.4
     */
    @NotNull
    public abstract String getName();

    /**
     * Copies all values of the other handler to this handler.
     *
     * @param handler The handler to copy values from.
     * @since 0.3.2
     */
    public abstract void mergeHandler(@NotNull final INBTHandler<?> handler);
}
