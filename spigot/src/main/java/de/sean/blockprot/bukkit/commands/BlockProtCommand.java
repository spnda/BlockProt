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

package de.sean.blockprot.bukkit.commands;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.BlockProtAPI;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.inventories.InventoryState;
import de.sean.blockprot.bukkit.inventories.StatisticsInventory;
import de.sean.blockprot.bukkit.inventories.UserSettingsInventory;
import de.sean.blockprot.bukkit.tasks.UpdateChecker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class BlockProtCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        switch (args[0]) {
            case "update": {
                if (sender.isOp()) {
                    Bukkit.getScheduler().runTaskAsynchronously(
                        BlockProt.getInstance(),
                        new UpdateChecker(
                            BlockProt.getInstance().getDescription(),
                            new ArrayList<>(Bukkit.getOnlinePlayers())
                        )
                    );
                    return true;
                }
                break;
            }
            case "settings": {
                if (!(sender instanceof Player)) break;
                Player player = (Player) sender;
                InventoryState state = new InventoryState(null);
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH;
                InventoryState.set(player.getUniqueId(), state);
                player.openInventory(new UserSettingsInventory().fill(player));
                return true;
            }
            case "reload": {
                if (sender.isOp()) {
                    BlockProt.getInstance().reloadConfigAndTranslations();
                    sender.spigot().sendMessage(new TextComponent("Finished reloading BlockProt!"));
                    return true;
                }
                break;
            }
            case "stats":
            case "statistics": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command is only executable as a player!");
                    return false;
                }
                Player player = (Player) sender;

                final InventoryState state = new InventoryState(null);
                state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH;
                InventoryState.set(player.getUniqueId(), state);

                player.openInventory(new StatisticsInventory().fill(player));
                return true;
            }
            case "about": {
                final ComponentBuilder builder = new ComponentBuilder();
                final PluginDescriptionFile description = BlockProt.getInstance().getDescription();
                builder.append("§x§a§3§c§6§e§bBlockProt v" + description.getVersion() + " - Spigot Plugin\n");
                builder.append("Author: " +
                    Pattern.compile("[\\[\\]]")
                        .matcher(description.getAuthors().toString()).replaceAll("") + "\n"
                );
                builder.append(createUrlComponent("§x§c§3§e§e§a§7Click here to report issues or for suggestions\n", "https://github.com/spnda/BlockProt/issues", "You can report issues to me here!"));
                sender.spigot().sendMessage(builder.create());
                return true;
            }
            case "integrations": {
                List<PluginIntegration> integrations = BlockProtAPI.getInstance().getIntegrations();

                final ComponentBuilder builder = new ComponentBuilder();
                builder.append("§7Enabled integrations (" + integrations.size() + "): ");
                for (int i = 0; i < integrations.size(); i++) {
                    if (!integrations.get(i).isEnabled()) continue;

                    builder.append("§6" + integrations.get(i).name);
                    if (i < integrations.size() - 1)
                        builder.append("§7, ");
                }
                sender.spigot().sendMessage(builder.create());
                return true;
            }
        }

        return false;
    }

    private TextComponent createUrlComponent(@NotNull String text, @NotNull String url, String hoverText) {
        final TextComponent component = new TextComponent(text);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        if (hoverText != null)
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        return component;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            List<String> list = new ArrayList<>(Arrays.asList("settings", "stats", "about"));
            if (sender.isOp()) {
                list.add("update");
                list.add("reload");
                list.add("integrations");
            }
            return list;
        }

        return Collections.emptyList();
    }
}
