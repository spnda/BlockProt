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

package de.sean.blockprot.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockProtUtil {
    /**
     * Parse a comma-separated list from a {@link String}. For example {@code "entry, entry"}
     * would be valid and would return a {@link List} of {@link String}s with two entries,
     * both "entry".
     *
     * @param stringList The String we want to parse from.
     * @return The split up and parsed List.
     */
    @NotNull
    public static List<String> parseStringList(@Nullable final String stringList) {
        if (stringList == null) return Collections.emptyList();
        final String[] tempList =
            stringList
                .replaceAll("^\\[|]$", "")
                .split(", ");
        final List<String> ret = new ArrayList<>(Arrays.asList(tempList));
        ret.removeIf(String::isEmpty);
        return ret;
    }
}