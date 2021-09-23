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

package de.sean.blockprot.bukkit;

import de.sean.blockprot.bukkit.nbt.stats.PlayerStatHandler;
import de.sean.blockprot.bukkit.nbt.stats.StatHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

public final class StatisticManager {
    private BukkitTask updateTask;

    @Nullable
    static StatisticManager instance;

    static StatHandler statHandler;

    StatisticManager(BlockProt blockProt) {
        instance = this;
        try {
            statHandler = new StatHandler();
            updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                blockProt,
                new FileUpdateTask(),
                0L,
                5 * 60 * 20 // 5 minutes * 60 seconds * 20 ticks
            );
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to create statistic handler.");
            Bukkit.getLogger().warning(e.toString());
        }
    }

    public static void onDisable() {
        if (instance == null) return;
        try {
            statHandler.saveFile();
            instance.updateTask.cancel();
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save statistic file.");
            Bukkit.getLogger().warning(e.toString());
        }
    }

    public static void addContainer(@NotNull final String playerUuid, @NotNull final Block block) {
        statHandler.getServerStats().modifyContainerCount(1);

        Optional<PlayerStatHandler> playerStats = statHandler.getStatsForPlayer(playerUuid);
        if (!playerStats.isPresent()) {
            statHandler.addStatsForPlayer(playerUuid).addContainer(block.getLocation().toVector());
        } else {
            playerStats.get().addContainer(block.getLocation().toVector());
        }
    }

    public static void addContainer(@NotNull final Player player, @NotNull final Block block) {
        addContainer(player.getUniqueId().toString(), block);
    }

    public static void removeContainer(@NotNull final String playerUuid, @NotNull final Block block) {
        statHandler.getServerStats().modifyContainerCount(-1);

        Optional<PlayerStatHandler> playerStats = statHandler.getStatsForPlayer(playerUuid);
        if (!playerStats.isPresent()) {
            statHandler.addStatsForPlayer(playerUuid).addContainer(block.getLocation().toVector());
        }
    }

    public static void removeContainer(@NotNull final Player player, @NotNull final Block block) {
        removeContainer(player.getUniqueId().toString(), block);
    }

    private static class FileUpdateTask implements Runnable {
        @Override
        public void run() {
            try {
                statHandler.saveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
