/*
 * Copyright (C) 2021 - 2025 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.BlockProtAPI;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
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

        String names = enabledIntegrations.stream()
            .map(integration -> "§6" + integration.name)
            .reduce((left, right) -> left + "§7, " + right)
            .orElse("§8-");

        var builder = new ComponentBuilder();
        builder.append(Translator.get(TranslationKey.MESSAGES__INTEGRATIONS_ENABLED)
            .replace("{count}", String.valueOf(enabledIntegrations.size()))
            .replace("{integrations}", names));

        sender.spigot().sendMessage(builder.create());
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(@NotNull CommandSender sender) {
        return sender.isOp();
    }
}
