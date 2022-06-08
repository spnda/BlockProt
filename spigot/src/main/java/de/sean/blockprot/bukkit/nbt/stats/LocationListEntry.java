/*
 * Copyright (C) 2021 - 2022 spnda
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

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.nbt.stats.ListStatisticItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public class LocationListEntry extends ListStatisticItem<Location, Material> {
    public LocationListEntry(@NotNull Location value) {
        super(value);
    }

    public @NotNull Block getBlock() {
        return this.get().getBlock();
    }

    @Override
    public @NotNull Material getItemType() {
        return this.get().getBlock().getType();
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public String getTitle() {
        var name = new BlockNBTHandler(getBlock()).getName();
        var coordinates = new StringJoiner(", ", "[", "]")
            .add(String.valueOf(this.value.getBlockX()))
            .add(String.valueOf(this.value.getBlockY()))
            .add(String.valueOf(this.value.getBlockZ()))
            .toString();
        return name + " " + coordinates;
    }
}
