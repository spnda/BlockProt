/*
 * Copyright (C) 2021 - 2023 spnda
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

import org.jetbrains.annotations.NotNull;

/**
 * A base statistic backed by a single floating point number.
 * 
 * @since 1.0.0
 */
public abstract class FloatStatistic extends BukkitStatistic<Float> {
    @Override
    public @NotNull String toString() {
        return this.get().toString();
    }

    @Override
    public @NotNull Float get() {
        return this.container.getFloat(this.getKey());
    }

    @Override
    public void set(@NotNull Float value) {
        this.container.setFloat(this.getKey(), value);
    }

    @Override
    public int compareTo(@NotNull BukkitStatistic<Float> o) {
        return this.get().compareTo(o.get());
    }
}
