package de.sean.blockprot.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A semantic versioning helper class to compare two versions
 */
public class SemanticVersion implements Comparable<SemanticVersion> {
    /**
     * The semantic version String separated by the dots.
     */
    private final String[] parts;

    public SemanticVersion(@NotNull String version) {
        parts = version.split("\\.");
    }

    /**
     * Compares this version with another semantic version. If parsing
     * failed with either this or other this will silently return 0.
     * @param other The other semantic version to compare against.
     * @return 1 if this is newer than {@code other} and -1 if {@code other}
     * is newer. 0 if the same or parsing failed.
     */
    @Override
    public int compareTo(@NotNull SemanticVersion other) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SemanticVersion)) return false;
        return this.compareTo((SemanticVersion)obj) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }
}
