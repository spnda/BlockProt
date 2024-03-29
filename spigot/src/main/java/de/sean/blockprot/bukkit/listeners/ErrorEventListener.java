/*
 * Copyright (C) 2021 - 2023 spnda
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
    private static boolean hasSentVersionMismatch = false;
    private static boolean hasSentCBWarning = false;

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && !hasSentVersionMismatch) {
            event.getPlayer().sendMessage("§c[BlockProt]: This plugin does not support the current Minecraft version."
                + "Please check if there is a new update available");
            hasSentVersionMismatch = true;
        }

        if (event.getPlayer().isOp() && !hasSentCBWarning) {
            event.getPlayer().sendMessage("§c[BlockProt]: This plugin does not work on CraftBukkit servers, "
                + "as they are only meant for development purposes. Please use a Spigot server instead.");
            hasSentCBWarning = true;
        }
    }
}
