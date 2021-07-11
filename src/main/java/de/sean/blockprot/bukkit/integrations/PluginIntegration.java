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

import de.sean.blockprot.bukkit.BlockProt;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A plugin integration that can register new event listeners and
 * add functionality to bring compatibility with another Bukkit
 * plugin.
 *
 * @since 0.4.0
 */
public abstract class PluginIntegration {
    /**
     * The {@link YamlConfiguration} that backs this plugin
     * integration. The name supplied in {@link #PluginIntegration(String)}
     * is used as the filename to read this configuration.
     *
     * @since 0.4.0
     */
    @NotNull
    protected final YamlConfiguration configuration;

    /**
     * {@link BlockProt}'s plugin manager to use to get the
     * instance of the plugin this is trying to integrate.
     *
     * @since 0.4.0
     */
    @NotNull
    private final PluginManager pluginManager;

    /**
     * Creates a new plugin integration.
     *
     * @param name The name of the integration and also the name of the config file, excluding the file ending.to integrate.
     * @since 0.4.0
     */
    public PluginIntegration(@NotNull final String name) {
        configuration =
            BlockProt.getInstance().saveAndLoadConfigFile(name, false);
        pluginManager = BlockProt.getInstance().getServer().getPluginManager();
    }

    /**
     * This lets all registered plugin integrations filter out friends that
     * they don't want players to add to {@code block}.
     *
     * @param friends The initial (default) list of friends that can be added.
     * @param player  The player that is trying to add these friends.
     * @param block   The block these friends are being added to.
     * @return The new filtered list.
     * @since 0.4.0
     */
    public static ArrayList<OfflinePlayer> filterFriends(@NotNull final ArrayList<OfflinePlayer> friends,
                                                         @NotNull final Player player,
                                                         @NotNull final Block block) {
        for (PluginIntegration integration : BlockProt.getInstance().getIntegrations()) {
            integration.filterFriendsInternal(friends, player, block);
        }
        return friends;
    }

    /**
     * Checks whether or not this plugin integration is currently enabled.
     *
     * @return Boolean if this integration is enabled.
     * @since 0.4.0
     */
    public abstract boolean isEnabled();

    /**
     * Load and setup this plugin integration. Should only be called
     * once.
     *
     * @since 0.4.0
     */
    public abstract void load();

    /**
     * A integration can freely override this function to change the friends
     * that can be added for a {@code block} by {@code player}.
     *
     * @param friends The initial (default) list of friends that can be added.
     * @param player  The player that is trying to add these friends.
     * @param block   The block these friends are being added to.
     * @since 0.4.0
     */
    protected void filterFriendsInternal(@NotNull final ArrayList<OfflinePlayer> friends,
                                         @NotNull final Player player,
                                         @NotNull final Block block) {
    }

    /**
     * Register any Bukkit event listener.
     *
     * @param listener The listener we want to register.
     * @since 0.4.0
     */
    protected void registerListener(@NotNull final Listener listener) {
        pluginManager.registerEvents(listener, BlockProt.getInstance());
    }
}
