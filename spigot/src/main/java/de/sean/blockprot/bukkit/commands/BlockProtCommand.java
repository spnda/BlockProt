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

package de.sean.blockprot.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Wrapper around all commands for BlockProt. It implements the tap completion and command
 * functions for ease of use, based on a public map of command executors.
 *
 * @since 1.1.2
 */
public final class BlockProtCommand implements TabExecutor {
    static Map<String, CommandExecutor> tabExecutors = new HashMap<>();

    static {
        var statsCommand = new StatisticsCommand();
        tabExecutors.put("stats", statsCommand);
        tabExecutors.put("statistics", statsCommand);
        tabExecutors.put("settings", new SettingsCommand());
        tabExecutors.put("about", new AboutCommand());
        tabExecutors.put("update", new UpdateCommand());
        tabExecutors.put("reload", new ReloadCommand());
        tabExecutors.put("integrations", new IntegrationsCommand());
        tabExecutors.put("debug", new DebugCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        var executor = tabExecutors.get(args[0]);
        if (executor != null) {
            return executor.onCommand(sender, command, label, args);
        }
        return false;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            final var list = new ArrayList<String>();
            for (var entry : tabExecutors.entrySet()) {
                if (entry.getValue().canUseCommand(sender))
                    list.add(entry.getKey());
            }
            return list;
        } else {
            var executor = tabExecutors.get(args[0]);
            if (executor != null) {
                final var completions = executor.onTabComplete(sender, command, alias, args);
                if (completions != null) {
                    return completions;
                }
            }
        }

        return Collections.emptyList();
    }
}
