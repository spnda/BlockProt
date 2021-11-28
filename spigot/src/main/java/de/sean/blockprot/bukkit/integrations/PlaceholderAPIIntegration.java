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
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.bukkit.nbt.StatHandler;
import de.sean.blockprot.bukkit.nbt.stats.BlockCountStatistic;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class PlaceholderAPIIntegration extends PluginIntegration {
    private BlockProtExpansion expansion;
    private boolean enabled = false;

    public PlaceholderAPIIntegration() {
        super("placeholderapi");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void enable() {
        final Plugin papi = getPlugin();
        if (papi == null || !papi.isEnabled()) return;

        this.expansion = new BlockProtExpansion();
        this.expansion.register();
        if (this.expansion.isRegistered()) {
            enabled = true;
        }
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("PlaceholderAPI");
    }

    private static final class BlockProtExpansion extends PlaceholderExpansion {
        @Override
        public boolean persist() {
            return true;
        }

        /**
         * Essentially a prefix to every placeholder we serve.
         * Cannot contain any underscores.
         */
        @Override
        public @NotNull String getIdentifier() {
            return "blockprot";
        }

        @Override
        public @NotNull String getAuthor() {
            return BlockProt.getInstance().getDescription().getAuthors().toString();
        }

        @Override
        public @NotNull String getVersion() {
            return BlockProt.getInstance().getDescription().getVersion();
        }

        @Override
        public String onRequest(final OfflinePlayer player, @NotNull final String identifier) {
            if (player == null) return "";

            switch (identifier) {
                case "global_block_count":
                    BlockCountStatistic blockCountStatistic = new BlockCountStatistic();
                    StatHandler.getStatistic(blockCountStatistic);
                    return blockCountStatistic.get().toString();
                default:
                    return null;
            }
        }

        @Override
        public String onPlaceholderRequest(final Player player, @NotNull final String identifier) {
            if (player == null) return null;

            switch (identifier) {
                case "default_friends":
                    List<FriendHandler> players = (new PlayerSettingsHandler(player)).getFriends();
                    return players
                        .stream()
                        .map(FriendHandler::getName)
                        .collect(Collectors.toList())
                        .toString();
                case "own_block_count":
                    PlayerBlocksStatistic playerBlocksStatistic = new PlayerBlocksStatistic();
                    StatHandler.getStatistic(playerBlocksStatistic, player);
                    return playerBlocksStatistic.get().toString();
                default:
                    return null;
            }
        }
    }
}
