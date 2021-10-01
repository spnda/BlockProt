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
import de.sean.blockprot.nbt.stats.OnClickAction;
import de.sean.blockprot.nbt.stats.Statistic;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public abstract class BukkitStatistic<T> extends NBTHandler<NBTCompound> implements Statistic<T, NBTCompound, Material> {
    @Override
    public void updateContainer(NBTCompound container) {
        this.container = container;
    }

    @Override
    public @NotNull Material getItemType() {
        return Material.DIRT;
    }

    @Override
    public @NotNull OnClickAction getClickAction() {
        return OnClickAction.NONE;
    }
}
