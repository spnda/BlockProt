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

import de.sean.blockprot.bukkit.nbt.stats.Statistic;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Server/Global statistics.
 */
public final class ServerStatHandler extends NBTHandler<NBTCompound> {
    public ServerStatHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
    }

    public void getStatistic(final @NotNull Statistic<?> statistic) {
        statistic.container = this.container;
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {

    }
}
