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

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.bukkit.nbt.stats.BukkitStatistic;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.jetbrains.annotations.NotNull;

final class PlayerStatHandler extends NBTHandler<NBTCompound> {
    public PlayerStatHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public String getName() {
        String name = container.getName();
        return name == null ? "" : name;
    }

    public void getStatistic(final @NotNull BukkitStatistic<?> statistic) {
        statistic.updateContainer(this.container);
    }
}
