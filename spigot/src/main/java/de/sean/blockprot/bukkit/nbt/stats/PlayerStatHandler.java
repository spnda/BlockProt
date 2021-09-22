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
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class PlayerStatHandler extends NBTHandler<NBTCompound> {
    static final String CONTAINER_LIST_KEY = "containers";

    public PlayerStatHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.3.0
     */
    @NotNull
    public String getName() {
        String name = container.getName();
        return name == null ? "" : name;
    }

    private @NotNull Vector getVectorFromCompound(@NotNull NBTCompound c) {
        return new Vector(c.getDouble("x"), c.getDouble("y"), c.getDouble("z"));
    }

    /**
     * Returns a list of the coordinates of every container this player
     * owns.
     */
    public List<Vector> getContainers() {
        NBTCompoundList list = container.getCompoundList(CONTAINER_LIST_KEY);
        return list
            .stream()
            // Nbtapi pls implement
            .map(this::getVectorFromCompound)
            .collect(Collectors.toList());
    }

    public int getContainerCount() {
        return getContainers().size();
    }

    public void removeContainer(Vector vector) {
        NBTCompoundList list = container.getCompoundList(CONTAINER_LIST_KEY);
        for (int i = 0; i < list.size(); ++i) {
            if (getVectorFromCompound(list.get(i)).equals(vector)) {
                list.remove(i);
                break;
            }
        }
    }

    public void addContainer(Vector vector) {
        NBTListCompound compound = container.getCompoundList(CONTAINER_LIST_KEY).addCompound();
        compound.setDouble("x", vector.getX());
        compound.setDouble("y", vector.getY());
        compound.setDouble("z", vector.getZ());
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {

    }
}
