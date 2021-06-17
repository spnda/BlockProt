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

import org.bukkit.Bukkit;

public final class BlockProtMessenger {
    private BlockProtMessenger() {
    }

    public static void log(final String content, final LogSeverity severity) {
        switch (severity) {
            case LOG:
                Bukkit.getLogger().info(content);
                break;
            case WARN:
                Bukkit.getLogger().warning(content);
                break;
            default:
                break;
        }
    }

    public enum LogSeverity {
        LOG,
        WARN
    }
}
