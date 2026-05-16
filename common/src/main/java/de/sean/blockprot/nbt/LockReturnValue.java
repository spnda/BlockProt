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

package de.sean.blockprot.nbt;

import org.jetbrains.annotations.Nullable;

/**
 * A class representing a return value for any of the lock functions,
 * containing a success boolean and a potential error message.
 *
 * @since 0.1.10
 */
public final class LockReturnValue {
    public final boolean success;

    @Nullable
    public final Reason reason;

    public LockReturnValue(final boolean success, @Nullable final Reason reason) {
        this.success = success;
        this.reason = reason;
    }

    public enum Reason {
        NO_PERMISSION,
        EXCEEDED_MAX_BLOCK_COUNT
    }
}
