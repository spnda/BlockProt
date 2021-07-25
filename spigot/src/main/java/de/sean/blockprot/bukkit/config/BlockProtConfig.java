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

package de.sean.blockprot.bukkit.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a basic YAML configuration file with some
 * basic utility functions for manipulating common config
 * data.
 */
public abstract class BlockProtConfig {
    /**
     * The FileConfiguration backing this {@link BlockProtConfig} wrapper.
     */
    @NotNull
    protected final FileConfiguration config;

    /**
     * Create a new config.
     *
     * @param config The {@link FileConfiguration} to use.
     */
    public BlockProtConfig(@NotNull final FileConfiguration config) {
        this.config = config;
    }

    /**
     * Checks whether or not the given {@code list} contains the {@code query} String. It checks
     * each item using {@link String#equalsIgnoreCase(String)}.
     *
     * @param list  The list of strings to check.
     * @param query The string to compare to.
     * @return True, if any item of {@code list} qualifies for {@link String#equalsIgnoreCase(String)}
     * with {@code query}.
     */
    protected boolean listContainsIgnoreCase(@NotNull final List<String> list, @NotNull final String query) {
        for (String item : list) {
            if (item.equalsIgnoreCase(query)) return true;
        }
        return false;
    }

    /**
     * Filter a list of enum values by a list of names. The enum values are filtered
     * by {@code names}.
     *
     * @param enumValues The list of enum values we want to filter.
     * @param names      The list strings we want to filter by. Warning: This list will be modified.
     * @param <T>        The enum class.
     * @return A set of all {@code <T>} enum values that we found.
     */
    @NotNull
    protected <T extends Enum<?>> Set<T> loadEnumValuesByName(@NotNull final T[] enumValues, @NotNull final ArrayList<String> names) {
        final Set<T> ret = new HashSet<>();
        for (T value : enumValues) {
            if (listContainsIgnoreCase(names, value.name())) {
                ret.add(value);
                names.remove(value.name());
            }
        }
        if (!names.isEmpty()) {
            Bukkit.getLogger().warning("Failed to map following values to enum: " + names);
        }
        return ret;
    }
}
