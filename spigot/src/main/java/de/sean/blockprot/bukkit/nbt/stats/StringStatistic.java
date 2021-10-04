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

import org.jetbrains.annotations.NotNull;

/** A base helper class for a statistic backed by a single String */
public abstract class StringStatistic extends BukkitStatistic<String> {
    @Override
    public @NotNull String toString() {
        return this.get();
    }

    @Override
    public @NotNull String get() {
        return this.container.getString(this.getKey());
    }

    @Override
    public void set(@NotNull String value) {
        this.container.setString(this.getKey(), value);
    }
}
