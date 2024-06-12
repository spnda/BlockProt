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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Called when a player is trying to access a blocks lock menu.
 * Can be cancelled to prevent the inventory from opening up.
 * 
 * @since 1.0.0
 */
public final class BlockAccessMenuEvent extends BlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    private final Player player;

    @NotNull
    private final Set<MenuPermission> permissions = new HashSet<>();

    private boolean isCancelled = false;

    /**
     * @param block  The block that was placed.
     * @param player The player that placed the block.
     * @see BlockAccessMenuEvent
     */
    public BlockAccessMenuEvent(@NotNull final Block block,
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
     * The player that is trying to access the edit menu.
     *
     * @return The Bukkit player.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the permissions the player has when opening the
     * menu.
     */
    @NotNull
    public Set<MenuPermission> getPermissions() {
        return permissions;
    }

    /**
     * Adds a new permission. See {@link java.util.Collection#add(Object)}
     * for details on any exceptions and the implementation detail.
     *
     * @param permission The new permissions.
     */
    public void addPermission(@NotNull MenuPermission permission) {
        this.permissions.add(permission);
    }

    /**
     * Adds multiple permissions to this event.
     *
     * @param permissions The permissions do add.
     */
    public void addPermissions(@NotNull MenuPermission... permissions) {
        this.permissions.addAll(Arrays.asList(permissions));
    }

    /**
     * Removes a permission. See {@link java.util.Collection#add(Object)}
     * for details on any exceptions and the implementation detail.
     *
     * @param permission The permission to remove.
     */
    public void removePermission(@NotNull MenuPermission permission) {
        this.permissions.remove(permission);
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
     * The different permissions a player can have when opening
     * the menu. Each permission is specific to one element of the
     * menus and permission does not imply another unless explicitly
     * stated.
     */
    public enum MenuPermission {
        /* Allows a player to see the information of a block */
        INFO,
        /* Allows a player to lock or unlock a block */
        LOCK,
        /* Allows a player to manage redstone settings and friends */
        MANAGER
    }
}
