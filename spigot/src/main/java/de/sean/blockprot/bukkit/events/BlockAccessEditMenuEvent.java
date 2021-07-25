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
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is trying to access a blocks lock menu.
 * Can be cancelled to prevent the inventory from opening up.
 *
 * @since 0.4.0
 */
public final class BlockAccessEditMenuEvent extends BaseBlockEvent implements Cancellable {
    @NotNull
    private final Player player;

    private boolean isCancelled = false;

    @NotNull
    private MenuAccess access = MenuAccess.NORMAL;

    /**
     * @param block  The block that was placed.
     * @param player The player that placed the block.
     * @see BlockAccessEditMenuEvent
     * @since 0.4.0
     */
    public BlockAccessEditMenuEvent(@NotNull final Block block,
                                    @NotNull final Player player) {
        super(block);
        this.player = player;
    }

    /**
     * The player that is trying to access the edit menu.
     *
     * @return The Bukkit player.
     * @since 0.4.0
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the current level of {@link MenuAccess}.
     *
     * @return The current access level.
     * @since 0.4.0
     */
    @NotNull
    public MenuAccess getAccess() {
        return access;
    }

    /**
     * Sets new permissions. If {@code access} is lower than
     * the previous permissions, this call will be ignored.
     *
     * @param access The new permissions.
     * @since 0.4.0
     */
    public void setAccess(@NotNull MenuAccess access) {
        if (access.ordinal() > this.access.ordinal()) {
            this.access = access;
        }
    }

    /**
     * If this event is cancelled, the inventory will not be opened.
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * If this event is cancelled, the inventory will not be opened.
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.isCancelled = cancel;
    }

    /**
     * The level of access that a player can have.
     *
     * @since 0.4.0
     */
    public enum MenuAccess {
        NONE,
        INFO,
        NORMAL,
        ADMIN
    }
}
