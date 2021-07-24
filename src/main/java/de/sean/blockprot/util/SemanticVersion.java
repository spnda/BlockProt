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

import java.util.Arrays;

/**
 * A semantic versioning helper class to compare two versions.
 *
 * @since 0.1.11
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
    /**
     * The semantic version String separated by the dots.
     *
     * @since 0.1.11
     */
    private final String[] parts;

    /**
     * Creates a new SemanticVersion.
     *
     * @param version A version string in the semantic versioning format. E.g. 1.2.4.
     *                Only accepts digits.
     * @since 0.1.11
     */
    public SemanticVersion(@NotNull final String version) {
        parts = version.split("\\.");
    }

    /**
     * Compares this version with another semantic version. If parsing failed with either this or
     * other this will silently return 0.
     *
     * @param other The other semantic version to compare against.
     * @return 1 if this is newer than {@code other} and -1 if {@code other} is newer. 0 if the same
     * or parsing failed.
     * @since 0.1.11
     */
    @Override
    public int compareTo(@NotNull final SemanticVersion other) {
        try {
            int length = Math.min(parts.length, other.parts.length);
            for (int i = 0; i < length; i++) {
                int part = Integer.parseInt(parts[i]);
                int otherPart = Integer.parseInt(other.parts[i]);
                if (part < otherPart) return -1;
                if (part > otherPart) return 1;
            }
        } catch (Exception e) {
            // Current version might not be following semantic versioning.
            // If so, just print it to console if there are any issues parsing
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compares another object to this semantic version. If {@code obj} is null
     * or not an instance of {@link SemanticVersion}, this will simply return false.
     *
     * @param obj The other object to compare to.
     * @return true if {@code obj} is equals to this.
     * @since 0.1.11
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SemanticVersion)) {
            return false;
        }
        return this.compareTo((SemanticVersion) obj) == 0;
    }

    /**
     * Returns the hashcode for the individual digits of this version. For example,
     * 0.4.5 would be split up into an array of [0, 4, 5], of which this hashcode is
     * then generated using {@link Arrays#hashCode(Object[])}.
     *
     * @return The array hashcode of all the individual digits of this version.
     * @since 0.1.11
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.4.7
     */
    @Override
    public String toString() {
        return String.join(".", parts);
    }
}
