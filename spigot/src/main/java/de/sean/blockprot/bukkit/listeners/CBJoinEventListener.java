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

package de.sean.blockprot.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Simple {@link PlayerJoinEvent} listener, only activated
 * if a plain CraftBukkit server is used to warn thereof.
 */
public final class CBJoinEventListener implements Listener {
    public static boolean hasSent = false;

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && !hasSent) {
            event.getPlayer().sendMessage("§c[BlockProt]: This plugin does not work on CraftBukkit servers, "
                    + "as they are only meant for development purposes. Please use a Spigot server instead.");
            hasSent = true;
        }
    }
}
