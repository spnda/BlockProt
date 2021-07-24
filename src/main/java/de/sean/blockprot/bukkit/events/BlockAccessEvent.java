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
 * Called when a player is trying to access a block and its contents.
 * This event implements {@link Cancellable} and if cancelled, the
 * access to the block is blocked.
 *
 * @since 0.4.0
 */
public final class BlockAccessEvent extends BaseBlockEvent implements Cancellable {
    @NotNull
    private final Player player;

    private boolean isCancelled;

    /**
     * @param block  The block that was placed.
     * @param player The player that placed the block.
     * @see BlockAccessEvent
     * @since 0.4.0
     */
    public BlockAccessEvent(@NotNull final Block block,
                            @NotNull final Player player) {
        super(block);
        this.player = player;
    }

    /**
     * The player that is trying to access this block.
     *
     * @return The Bukkit player.
     * @since 0.4.0
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.isCancelled = cancel;
    }
}
