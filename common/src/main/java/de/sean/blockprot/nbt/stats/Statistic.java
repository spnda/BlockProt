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

package de.sean.blockprot.nbt.stats;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @param <V> The type of the value.
 * @param <C> The type of the container.
 * @param <M> The type of the item type.
 */
public interface Statistic<V, C, M> extends Comparable<Statistic<V, C, M>> {
    @NotNull String getKey();
    @NotNull StatisticType getType();
    @NotNull M getItemType();
    @Override @NotNull String toString();
    @NotNull V get();
    void set(@NotNull final V value);

    void updateContainer(C container);

    default @NotNull OnClickAction getClickAction() {
        return OnClickAction.NONE;
    }

    @Override
    default int compareTo(@NotNull Statistic<V, C, M> o) {
        return o.getKey().equals(this.getKey()) ? 0 : -1;
    }
}
