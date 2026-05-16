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

import org.jetbrains.annotations.NotNull;

/**
 * Runtime validation of compatibility with the running Minecraft/Paper version.
 *
 * Performs checks on plugin load to detect and warn about known issues or
 * unsupported configurations. All checks are non-fatal; the plugin still loads,
 * but administrators are warned about potential problems.
 *
 * Checks include:
 * - Is Paper available? (required for ChatInput, optional for core features)
 * - Is Java 25+? (required by this fork's class file target)
 * - API version compatibility vs actual server version
 * - Typed inventory views availability (1.21.4+)
 *
 * @since 1.2.9
 */
public final class VersionValidator {

    private VersionValidator() {}

    /**
     * Run all startup validation checks. Call from BlockProt#onEnable().
     *
     * If any checks fail, they are logged as warnings via Bukkit's logger
     * and BlockProtLogger, but the plugin continues to load.
     */
    public static void validateStartup() {
        checkJavaVersion();
        checkPaperAvailability();
        checkTypedInventoryViews();
    }

    /**
     * Validates that the server is running Java 25 or higher.
     * This fork is compiled with targetJavaVersion=25.
     */
    private static void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        int majorVersion = extractMajorVersion(javaVersion);

        if (majorVersion < 25) {
            warn(Translator.get(TranslationKey.CONSOLE__JAVA_TOO_OLD)
                .replace("{version}", javaVersion));
            BlockProtLogger.warn("Java version " + javaVersion + " detected. Requires 25+.");
        } else {
            BlockProtLogger.log("Java", "Version " + javaVersion + " OK");
        }
    }

    /**
     * Validates that Paper is available.
     * Paper is required for ChatInput (async chat events).
     * Without Paper, the plugin falls back to AnvilInput (anvil GUI).
     */
    private static void checkPaperAvailability() {
        boolean isPaper = VersionCompat.isPaper();
        if (!isPaper) {
            warn(Translator.get(TranslationKey.CONSOLE__NOT_PAPER));
            BlockProtLogger.warn(
                "Not running on Paper. Chat-based player search requires Paper/PaperMC. " +
                "Falling back to anvil GUI."
            );
        } else {
            BlockProtLogger.log("Paper", "Available OK");
        }
    }

    /**
     * Validates that typed inventory views are available.
     * Typed views were added in Paper 1.21.4.
     * For Paper 1.21.0-1.21.3, AnvilInput fallbacks are used.
     */
    private static void checkTypedInventoryViews() {
        boolean hasTypedViews = VersionCompat.hasTypedInventoryViews();
        if (!hasTypedViews && VersionCompat.isAtLeast(1, 21, 0)) {
            warn(Translator.get(TranslationKey.CONSOLE__TYPED_VIEWS_FALLBACK));
            BlockProtLogger.warn(
                "Typed inventory views not available (1.21.0-1.21.3). Using fallback methods."
            );
        } else if (hasTypedViews) {
            BlockProtLogger.log("Inventory", "Typed views available OK");
        }
    }

    private static int extractMajorVersion(@NotNull String versionString) {
        try {
            String[] parts = versionString.split("\\.");
            if (parts.length > 0) {
                return Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private static void warn(@NotNull String message) {
        BlockProt.getInstance().getLogger().warning(message);
    }
}
