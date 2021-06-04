/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.sean.blockprot.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A semantic versioning helper class to compare two versions.
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
    /**
     * The semantic version String separated by the dots.
     */
    private final String[] parts;

    /**
     * Creates a new SemanticVersion.
     *
     * @param version A version string in the semantic versioning format. E.g. 1.2.4.
     *                Only accepts digits.
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
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }
}
