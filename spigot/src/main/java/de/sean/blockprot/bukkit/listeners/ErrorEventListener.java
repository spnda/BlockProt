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

package de.sean.blockprot.bukkit.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Simple listener class we use to notify OPs of issues regarding the plugin,
 * like an incompatible version.
 */
public class ErrorEventListener implements Listener {
    private String errorMessage;

    public ErrorEventListener(@NotNull String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && !errorMessage.isEmpty()) {
            event.getPlayer().sendMessage("§c[BlockProt]: " + errorMessage);
            errorMessage = "";
        }
    }
}
