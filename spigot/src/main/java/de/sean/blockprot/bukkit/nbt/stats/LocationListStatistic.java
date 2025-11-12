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

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A statistic that keeps track of many {@link Location}s.
 * 
 * @since 1.0.0
 */
public abstract class LocationListStatistic extends BukkitListStatistic<LocationListEntry, Location> {
    private @NotNull Location parseLocationFromCompound(@NotNull ReadWriteNBT c) {
        return new Location(
            Bukkit.getWorld(c.getString("name")),
            c.getDouble("x"),
            c.getDouble("y"),
            c.getDouble("z"));
    }

    private void writeLocationToCompound(@NotNull NBTCompound compound, @NotNull Location location) {
        compound.setString("name", Objects.requireNonNull(location.getWorld()).getName());
        compound.setDouble("x", location.getX());
        compound.setDouble("y", location.getY());
        compound.setDouble("z", location.getZ());
    }

    @Override
    public @NotNull String toString() {
        return this.get().toString();
    }

    @Override
    public @NotNull List<LocationListEntry> get() {
        return getList()
            .stream()
            .map(this::parseLocationFromCompound)
            .map(LocationListEntry::new)
            .collect(Collectors.toList());
    }

    @Override
    public void set(@NotNull List<LocationListEntry> value) {
        container.removeKey(this.getKey());
        value.forEach(v -> this.add(v.get()));
    }

    @Override
    public void add(@NotNull Location vector) {
        writeLocationToCompound(getList().addCompound(), vector);
    }

    @Override
    public void remove(@NotNull Location object) {
        getList().removeIf(c -> parseLocationFromCompound(c).equals(object));
    }

    @Override
    public void remove(int index) {
        getList().remove(index);
    }
}
