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
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBlockEvent extends BlockEvent {
    /**
     * The internal list of handlers (listeners) for this event,
     * so that Bukkit/Spigot can differentiate between the different
     * events.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates a new BaseBlockEvent, essentially a {@link BlockEvent}
     * with integrated handlers.
     *
     * @param block The block that backs this event, which is passed
     *              directly to the {@link BlockEvent}.
     */
    public BaseBlockEvent(@NotNull final Block block) {
        super(block);
    }

    /**
     * Get the {@link HandlerList} for this event. Only useful for
     * Bukkit/Spigot's Event API.
     *
     * @return The handler list.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public final HandlerList getHandlers() {
        return HANDLERS;
    }
}
