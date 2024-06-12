/*
 * Copyright (C) 2021 - 2024 spnda
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is trying to access a block and its contents.
 * This event implements {@link Cancellable} and if cancelled, the
 * access to the block is blocked.
 *
 * @since 0.4.0
 */
public final class BlockAccessEvent extends BlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    private final Player player;

    private boolean isCancelled;
    private boolean bypassProtections;

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

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
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

    /**
     * Whether the plugin should still check if the requesting player
     * actually owns this block, or that the player has some privileges
     * to be able to ignore any protections.
     * 
     * @since 1.0.0
     */
    public boolean shouldBypassProtections() {
        return this.bypassProtections;
    }

    /**
     * Set whether the player can bypass protections. Use this with caution,
     * as some shady plugin might allow specific players to bypass any protections.
     * 
     * @see #shouldBypassProtections()
     * @since 1.0.0
     */
    public void setBypassProtections(final boolean bypass) {
        this.bypassProtections = bypass;
    }
}
