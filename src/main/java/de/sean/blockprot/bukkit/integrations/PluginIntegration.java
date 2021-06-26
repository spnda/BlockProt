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
package de.sean.blockprot.bukkit.integrations;

import de.sean.blockprot.BlockProt;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class PluginIntegration {
    @NotNull
    protected final YamlConfiguration configuration;

    @NotNull
    private final PluginManager pluginManager;

    public PluginIntegration(String name) {
        configuration =
            BlockProt.getInstance().saveAndLoadConfigFile(name, false);
        pluginManager = BlockProt.getInstance().getServer().getPluginManager();
    }

    public abstract boolean isEnabled();

    public abstract void load();

    /**
     * A integration can freely override this function to change the friends
     * that can be added for a {@code block} by {@code player}.
     * @param friends The initial (default) list of friends that can be added.
     * @param player The player that is trying to add these friends.
     * @param block The block these friends are being added to.
     */
    protected void filterFriendsInternal(ArrayList<OfflinePlayer> friends, Player player, Block block) {}

    /**
     * This lets all registered plugin integrations filter out friends that
     * they don't want players to add to {@code block}.
     * @param friends The initial (default) list of friends that can be added.
     * @param player The player that is trying to add these friends.
     * @param block The block these friends are being added to.
     */
    public static void filterFriends(ArrayList<OfflinePlayer> friends, Player player, Block block) {
        for (PluginIntegration integration : BlockProt.getInstance().getIntegrations()) {
            integration.filterFriendsInternal(friends, player, block);
        }
    }

    protected void registerListener(@NotNull final Listener listener) {
        pluginManager.registerEvents(listener, BlockProt.getInstance());
    }
}
