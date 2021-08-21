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
import de.sean.blockprot.bukkit.inventories.InventoryState;
import de.sean.blockprot.bukkit.inventories.UserSettingsScreen;
import de.sean.blockprot.bukkit.tasks.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                Player player = Bukkit.getPlayer(sender.getName());
                if (player != null) {
                    InventoryState state = new InventoryState(null);
                    state.friendSearchState = InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH;
                    InventoryState.set(player.getUniqueId(), state);
                    player.openInventory(new UserSettingsScreen().fill(player));
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            List<String> list = new ArrayList<>(Collections.singletonList("settings"));
            if (sender.isOp()) {
                list.add("update");
            }
            return list;
        }

        return Collections.emptyList();
    }
}
