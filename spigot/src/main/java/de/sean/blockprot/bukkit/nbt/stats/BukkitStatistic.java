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
import de.sean.blockprot.nbt.stats.StatisticOnClickAction;
import de.sean.blockprot.nbt.stats.StatisticType;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * A statistic.
 * @param <V> The type of the value, e.g. {@link Integer} or {@link String}.
 */
public abstract class BukkitStatistic<V> extends NBTHandler<NBTCompound> implements Comparable<BukkitStatistic<V>> {
    /** The key for getting the NBT for this statistic. */
    public abstract @NotNull String getKey();
    /** The type of this statistic. */
    public abstract @NotNull StatisticType getType();
    /** Get the (translated) name of this statistic. */
    public abstract String getStatisticName();
    public abstract @NotNull V get();
    public abstract void set(@NotNull V value);

    /** Update the NBT container from which this statistic shall be read. */
    public void updateContainer(@NotNull NBTCompound container) {
        this.container = container;
    }

    public @NotNull Material getItemType() {
        return Material.DIRT;
    }

    /** Get what should happen when the user clicks on this statistic. */
    public @NotNull StatisticOnClickAction getClickAction() {
        return StatisticOnClickAction.NONE;
    }

    /** Returns the formatted statistic name with the value of this statistic. */
    public @NotNull String getTitle() {
        return getStatisticName() + ": " + get().toString();
    }

    @Override
    public int compareTo(@NotNull BukkitStatistic<V> o) {
        return o.getKey().equals(this.getKey()) ? 0 : -1;
    }
}
