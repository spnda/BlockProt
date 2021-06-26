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
package de.sean.blockprot.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is trying to access a blocks lock menu.
 */
public class BlockAccessEditMenuEvent extends BaseBlockEvent {
    @NotNull
    private final Player player;

    @NotNull
    private MenuAccess access = MenuAccess.NORMAL;

    public BlockAccessEditMenuEvent(@NotNull Block block, @NotNull final Player player) {
        super(block);
        this.player = player;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets new permissions. If {@code access} is lower than
     * the previous permissions, this call will be ignored.
     * @param access The new permissions.
     */
    public void setAccess(@NotNull MenuAccess access) {
        if (access.ordinal() > access.ordinal()) {
            this.access = access;
        }
    }

    @NotNull
    public MenuAccess getAccess() {
        return access;
    }

    public enum MenuAccess {
        NONE,
        INFO,
        NORMAL,
        ADMIN;
    }
}
