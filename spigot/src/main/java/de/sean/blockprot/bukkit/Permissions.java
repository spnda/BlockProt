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

package de.sean.blockprot.bukkit;

/**
 * Represents the permission keys for BlockProt, handled by the Spigot server.
 * @since 1.1.7
 */
public enum Permissions {
    LOCK("blockprot.lock"),
    INFO("blockprot.info"),
    ADMIN("blockprot.admin"),
    BYPASS("blockprot.bypass");

    private final String text;

    Permissions(final String text) {
        this.text = text;
    }

    @Override
    public final String toString() {
        return text;
    }

    public final String key() {
        return text;
    }
}
