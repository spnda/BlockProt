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

import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class VectorListStatistic extends ListStatistic<Vector> {
    private @NotNull Vector getVectorFromCompound(@NotNull NBTCompound c) {
        return new Vector(c.getDouble("x"), c.getDouble("y"), c.getDouble("z"));
    }

    private void writeVectorToCompound(@NotNull NBTCompound compound, @NotNull Vector vector) {
        compound.setDouble("x", vector.getX());
        compound.setDouble("y", vector.getY());
        compound.setDouble("z", vector.getZ());
    }

    @Override
    public @NotNull String toString() {
        return this.get().toString();
    }

    @Override
    public @NotNull List<Vector> get() {
        return getList()
            .stream()
            .map(this::getVectorFromCompound)
            .collect(Collectors.toList());
    }

    @Override
    public void set(@NotNull List<Vector> value) {
        container.removeKey(this.getKey());
        value.forEach(this::add);
    }

    @Override
    public void add(Vector vector) {
        writeVectorToCompound(getList().addCompound(), vector);
    }

    @Override
    public void remove(Vector object) {
        getList().removeIf(c -> getVectorFromCompound(c).equals(object));
    }

    @Override
    public void remove(int index) {
        getList().remove(index);
    }
}
