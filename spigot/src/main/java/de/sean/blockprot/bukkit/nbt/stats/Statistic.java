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

import de.sean.blockprot.bukkit.nbt.NBTHandler;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public abstract class Statistic<T> extends NBTHandler<NBTCompound> implements Comparable<Statistic<T>> {
    public abstract @NotNull String getKey();
    public abstract @NotNull StatisticType getType();
    public abstract @NotNull NBTType getNbtType();
    public abstract @NotNull T get();
    public abstract void set(@NotNull T value);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract @NotNull String toString();

    public final void setContainer(NBTCompound compound) {
        this.container = compound;
    }

    @Override
    public int compareTo(@NotNull Statistic<T> o) {
        return o.getKey().equals(this.getKey()) ? 0 : -1;
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) throws NotImplementedException {
        if (!(handler instanceof Statistic)) return;
        throw new NotImplementedException();
    }

    public enum StatisticType {
        PLAYER, GLOBAL, ALL
    }
}
