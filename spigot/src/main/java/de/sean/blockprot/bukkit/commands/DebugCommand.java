/*
 * Copyright (C) 2021 - 2022 spnda
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

package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("blockprot.debug"))
            return false;

        switch (args[1]) {
            case "placeDebugChest" -> {
                if (!(sender instanceof Player player)) break;
                player.getWorld().setType(player.getLocation(), Material.CHEST);
                final var handler = new BlockNBTHandler(player.getWorld().getBlockAt(player.getLocation()));
                handler.setOwner("069a79f4-44e9-4726-a5be-fca90e38aaf5"); // Notch's UUID.
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("blockprot.debug"))
            return Collections.emptyList();

        return new ArrayList<>(Arrays.asList("placeDebugChest"));
    }

    @Override
    public boolean canUseCommand(@NotNull CommandSender sender) {
        return sender.hasPermission("blockprot.debug");
    }
}
