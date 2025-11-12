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

/**
 * @since 0.1.16
 */
public enum FriendModifyAction {
    /**
     * We want to add a friend to the block or to the
     * default friends for a player.
     *
     * @since 0.1.16
     */
    ADD_FRIEND,

    /**
     * We want to remove a friend to the block or to the
     * default friends for a player.
     *
     * @since 0.1.16
     */
    REMOVE_FRIEND
}
