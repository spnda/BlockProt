/*
 * Copyright (C) 2021 - 2025 spnda
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

import de.sean.blockprot.nbt.stats.ListStatisticItem;
import de.sean.blockprot.nbt.stats.StatisticOnClickAction;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A statistic that can keep a list of items.
 * 
 * @since 1.0.0
 */
public abstract class BukkitListStatistic<V extends ListStatisticItem<IV, Material>, IV>
    extends BukkitStatistic<List<V>> {
    public abstract void add(@NotNull IV object);
    public abstract void remove(int index);
    public abstract void remove(@NotNull IV object);

    public @NotNull Material getItemType() {
        return Material.DIRT;
    }

    public @NotNull StatisticOnClickAction getClickAction() {
        return StatisticOnClickAction.LIST_MENU;
    }

    protected @NotNull NBTCompoundList getList() {
        return container.getCompoundList(this.getKey());
    }

    @Override
    public @NotNull String getTitle() {
        return getStatisticName();
    }
}
