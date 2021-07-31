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

package de.sean.blockprot.nbt;

import org.jetbrains.annotations.NotNull;

/**
 * A class representing a return value for any of the lock functions,
 * containing a success boolean and a potential error message.
 *
 * @since 0.1.10
 */
public final class LockReturnValue {
    /**
     * Whether the operation completed successfully.
     *
     * @since 0.2.3
     */
    public final boolean success;

    /**
     * An error/success message for the player.
     *
     * @since 0.2.3
     */
    @Deprecated
    @NotNull
    public final String message;

    /**
     * Create a new lock return value.
     *
     * @param success Whether the operation completed successfully.
     * @since 0.4.9
     */
    public LockReturnValue(final boolean success) {
        this.success = success;
        this.message = "";
    }

    /**
     * Creates a new LockReturnValue with given values.
     *
     * @param success Whether the operation completed successfully.
     * @param message An error/success message for the player.
     * @since 0.1.10
     * @deprecated since 0.4.9. Use {@link #LockReturnValue(boolean)} instead.
     */
    @Deprecated
    public LockReturnValue(final boolean success, @NotNull final String message) {
        this.success = success;
        this.message = message;
    }
}
