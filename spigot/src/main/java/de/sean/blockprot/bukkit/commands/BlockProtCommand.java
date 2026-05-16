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

package de.sean.blockprot.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Wrapper around all commands for BlockProt. Implements tab-completion and dispatch
 * for all registered sub-command executors.
 *
 * @since 1.1.2
 */
public final class BlockProtCommand implements TabExecutor {

    private static final Map<String, CommandExecutor> canonicalExecutors = new LinkedHashMap<>();
    private static final Map<String, List<String>> englishAliases = new LinkedHashMap<>();

    static {
        var statsCommand = new StatisticsCommand();
        register("stats",        statsCommand, "statistics");
        register("settings",     new SettingsCommand());
        register("about",        new AboutCommand());
        register("update",       new UpdateCommand());
        register("reload",       new ReloadCommand());
        register("integrations", new IntegrationsCommand());
        register("debug",        new DebugCommand());
        register("disablehints", new HintsCommand());
        register("friends",      new FriendsAddAllCommand());
        var helpCommand = new HelpCommand();
        register("help",         helpCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;

        var executor = buildExecutorMap().get(args[0].toLowerCase(Locale.ROOT));
        if (executor != null) {
            return executor.onCommand(sender, command, label, args);
        }
        return false;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            final var list = new ArrayList<String>();
            for (var entry : buildExecutorMap().entrySet()) {
                if (entry.getValue().canUseCommand(sender))
                    list.add(entry.getKey());
            }
            return list;
        }

        var executor = buildExecutorMap().get(args[0].toLowerCase(Locale.ROOT));
        if (executor != null) {
            final var completions = executor.onTabComplete(sender, command, alias, args);
            if (completions != null) return completions;
        }

        return Collections.emptyList();
    }

    private static void register(@NotNull String canonical, @NotNull CommandExecutor executor, String... aliases) {
        canonicalExecutors.put(canonical, executor);
        englishAliases.put(canonical, List.of(aliases));
    }

    @NotNull
    private static Map<String, CommandExecutor> buildExecutorMap() {
        Map<String, CommandExecutor> result = new LinkedHashMap<>();
        for (var entry : canonicalExecutors.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
            for (String alias : englishAliases.getOrDefault(entry.getKey(), Collections.emptyList())) {
                result.put(alias.toLowerCase(Locale.ROOT), entry.getValue());
            }
        }
        return result;
    }
}
