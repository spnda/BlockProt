/*
 * Copyright (C) 2021 - 2025 spnda / SP26 fork
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

package de.sean.blockprot.bukkit;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Runtime Minecraft version detection utility.
 *
 * <p>Works across the classic 1.x numbering and the new year-based
 * numbering introduced in 2026 (26.x, 27.x, …).
 *
 * <p>Usage:
 * <pre>{@code
 * if (VersionCompat.isAtLeast(1, 21, 4)) { ... }
 * if (VersionCompat.is26Family())        { ... }
 * }</pre>
 *
 * @since 1.2.9
 */
public final class VersionCompat {

    // -------------------------------------------------------------------------
    // Parsed version fields, set once at class-load time.
    // -------------------------------------------------------------------------

    /**
     * Whether the server is running the new year-based version scheme (26.x, 27.x, …).
     * Under the new scheme the first segment is a two-digit year (≥ 26), not "1".
     */
    public static final boolean NEW_SCHEME;

    /** Major segment of the version (1 for 1.21.x, 26 for 26.1.x). */
    public static final int MAJOR;
    /** Minor segment (21 for 1.21.x, 1 for 26.1.x). */
    public static final int MINOR;
    /** Patch / hotfix segment (4 for 1.21.4, 2 for 26.1.2). 0 if absent. */
    public static final int PATCH;

    static {
        // Bukkit.getMinecraftVersion() returns e.g. "1.21.4" or "26.1.2"
        String raw = Bukkit.getMinecraftVersion();
        String[] parts = raw.split("\\.");

        int major = 0, minor = 0, patch = 0;
        try {
            if (parts.length >= 1) major = Integer.parseInt(parts[0]);
            if (parts.length >= 2) minor = Integer.parseInt(parts[1]);
            if (parts.length >= 3) patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ignored) {}

        MAJOR = major;
        MINOR = minor;
        PATCH = patch;
        // Year-based scheme: first segment ≥ 26 AND ≤ 99 (avoids mistaking a 1.x build)
        NEW_SCHEME = (major >= 26 && major <= 99);
    }

    private VersionCompat() {}

    // -------------------------------------------------------------------------
    // Comparison helpers
    // -------------------------------------------------------------------------

    /**
     * Returns true if the running server version is at least {@code major.minor.patch}.
     *
     * <p>For year-based versions (26.x) the comparison still works correctly
     * because MAJOR will be 26, which is greater than any 1.x MAJOR.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code isAtLeast(1, 21, 0)} → true on 1.21, 1.21.4, 26.1, …
     *   <li>{@code isAtLeast(1, 21, 4)} → true on 1.21.4, 26.1, …; false on 1.21.1
     *   <li>{@code isAtLeast(26, 1, 0)} → true only on 26.1 and later
     * </ul>
     */
    public static boolean isAtLeast(int major, int minor, int patch) {
        if (MAJOR != major) return MAJOR > major;
        if (MINOR != minor) return MINOR > minor;
        return PATCH >= patch;
    }

    /** Convenience: at least major.minor, ignoring patch. */
    public static boolean isAtLeast(int major, int minor) {
        return isAtLeast(major, minor, 0);
    }

    /**
     * Returns true if the server is running the 26.x version family
     * (Minecraft 26.1 "Tiny Takeover" and later year-based releases).
     */
    public static boolean is26Family() {
        return NEW_SCHEME;
    }

    /**
     * Returns true if the server is on Paper 1.21.4 or later.
     * This is relevant because 1.21.4 is when Paper hard-forked from Spigot
     * and introduced typed inventory views ({@code InventoryView} became an interface).
     */
    public static boolean hasTypedInventoryViews() {
        // Typed inventory views: 1.21.4+ (classic) or any 26.x build.
        return isAtLeast(1, 21, 4);
    }

    /**
     * Returns true if the running server reports it is a Paper build.
     * Falls back to false if the Paper-specific class is not on the classpath.
     */
    public static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // String helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a human-readable version string, e.g. "1.21.4" or "26.1.2".
     */
    @NotNull
    public static String getVersionString() {
        return Bukkit.getMinecraftVersion();
    }

    /**
     * Returns a one-line diagnostic string suitable for logging.
     * Example: "MC 26.1.2 (26.x family, Paper)"
     */
    @NotNull
    public static String getDiagnosticString() {
        return String.format("MC %s (%s, %s)",
            getVersionString(),
            NEW_SCHEME ? "26.x year-based family" : "1.x classic",
            isPaper() ? "Paper" : "Spigot"
        );
    }
}
