/*
 * Copyright (C) 2021 - 2025 spnda
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

package de.sean.blockprot.nbt.stats;

import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0.0
 */
public abstract class ListStatisticItem<T, R> {
    protected final @NotNull T value;

    protected ListStatisticItem(@NotNull T value) {
        this.value = value;
    }

    public @NotNull T get() {
        return value;
    }

    public abstract @NotNull R getItemType();

    public abstract String getTitle();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
