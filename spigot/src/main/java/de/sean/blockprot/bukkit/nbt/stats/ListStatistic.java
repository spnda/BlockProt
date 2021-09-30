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

package de.sean.blockprot.bukkit.nbt.stats;

import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ListStatistic<T> extends Statistic<List<T>> {
    @Override
    public @NotNull NBTType getNbtType() {
        return NBTType.NBTTagList;
    }

    protected @NotNull NBTCompoundList getList() {
        return container.getCompoundList(this.getKey());
    }

    public abstract void add(T object);
    public abstract void remove(int index);
    public abstract void remove(T object);
}
