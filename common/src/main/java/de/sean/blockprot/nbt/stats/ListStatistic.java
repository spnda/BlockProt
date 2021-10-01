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

import java.util.List;

/**
 * @param <C> The type of the container.
 * @param <M> The type of the item type.
 * @param <T> The type of the custom list item, inheriting from {@link ListStatisticItem}.
 * @param <IV> The type of the item value.
 */
public interface ListStatistic<C, M, IV, T extends ListStatisticItem<IV, M>> extends Statistic<List<T>, C, M> {
    void add(IV object);
    void remove(int index);
    void remove(IV object);
}
