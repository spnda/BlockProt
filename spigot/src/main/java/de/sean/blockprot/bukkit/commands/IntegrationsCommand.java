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

package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.BlockProtAPI;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegrationsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!canUseCommand(sender))
            return false;

        var enabledIntegrations = BlockProtAPI.getInstance().getIntegrations().stream()
            .filter(PluginIntegration::isEnabled)
            .toList();

        var builder = new ComponentBuilder();
        builder.append("§7Enabled integrations (" + enabledIntegrations.size() + "): ");

        for (int i = 0; i < enabledIntegrations.size(); ++i) {
            builder.append("§6" + enabledIntegrations.get(i).name);
            if (i < enabledIntegrations.size() - 1)
                builder.append("§7, ");
        }

        sender.spigot().sendMessage(builder.create());
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(@NotNull CommandSender sender) {
        return sender.isOp();
    }
}
