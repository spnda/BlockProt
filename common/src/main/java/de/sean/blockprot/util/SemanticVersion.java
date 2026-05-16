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

package de.sean.blockprot.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * A semantic versioning helper class to compare two versions.
 *
 * @since 0.1.11
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
    private final String[] parts;
    private final String extension;

    public SemanticVersion(@NotNull final String version) {
        final String[] versionParts = version.split("-");
        parts = versionParts[0].split("\\.");
        extension = versionParts.length == 2 ? versionParts[1] : "";
    }

    @Override
    public int compareTo(@NotNull final SemanticVersion other) {
        try {
            boolean preRelease = extension.contains("alpha") || extension.contains("beta");
            int length = Math.min(parts.length, other.parts.length);
            for (int i = 0; i < length; i++) {
                int part = Integer.parseInt(parts[i]);
                int otherPart = Integer.parseInt(other.parts[i]);
                if (part < otherPart) return -1;
                if (part > otherPart && !preRelease) return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SemanticVersion)) return false;
        return this.compareTo((SemanticVersion) obj) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(parts), extension);
    }

    @Override
    public String toString() {
        return String.join(".", parts) + (extension.isEmpty() ? "" : "-" + extension);
    }
}
