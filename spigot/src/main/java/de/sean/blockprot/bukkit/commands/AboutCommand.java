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

import de.sean.blockprot.bukkit.BlockProt;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class AboutCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final ComponentBuilder builder = new ComponentBuilder();
        final PluginDescriptionFile description = BlockProt.getInstance().getDescription();
        builder.append("§x§a§3§c§6§e§bBlockProt v" + description.getVersion() + " - Spigot Plugin\n");
        builder.append("Author: " +
            Pattern.compile("[\\[\\]]")
                .matcher(description.getAuthors().toString()).replaceAll("") + "\n"
        );
        builder.append(createUrlComponent("§x§c§3§e§e§a§7Click here to report issues or for suggestions", "https://github.com/spnda/BlockProt/issues", "You can report issues to me here!"));
        sender.spigot().sendMessage(builder.create());
        return true;
    }

    private @NotNull TextComponent createUrlComponent(@NotNull String text, @NotNull String url, String hoverText) {
        final TextComponent component = new TextComponent(text);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        if (hoverText != null)
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        return component;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(@NotNull CommandSender sender) {
        return true;
    }
}
