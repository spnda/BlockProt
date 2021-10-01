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

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.nbt.stats.ContainerCountStatistic;
import de.sean.blockprot.bukkit.nbt.stats.PlayerContainersStatistic;
import de.sean.blockprot.bukkit.tasks.StatisticFileSaveTask;
import de.sean.blockprot.nbt.stats.Statistic;
import de.sean.blockprot.nbt.stats.StatisticType;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * StatHandler for statistic related NBT data.
 */
public final class StatHandler {
    static final String STAT_FILE_NAME = "blockprot_stats.nbt";
    static final String PLAYER_SUB_KEY = "player_stats";
    static final String SERVER_SUB_KEY = "server_stats";

    private static @Nullable BukkitTask fileSaveTask;
    private static @Nullable NBTFile nbtFile;

    public static void init() {
        if (nbtFile != null) return;
        try {
            // If there's no worlds on this server, that's not our issue.
            World world = Bukkit.getServer().getWorlds().get(0);
            final File file = new File(world.getWorldFolder(), STAT_FILE_NAME);
            nbtFile = new NBTFile(file);

            fileSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                BlockProt.getInstance(),
                new StatisticFileSaveTask(),
                0L,
                5 * 60 * 20 // 5 minutes * 60 seconds * 20 ticks
            );
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to open BlockProt statistic file.");
        }
    }

    public static void saveFile() throws IOException {
        if (nbtFile != null) nbtFile.save();
    }

    public static void disable() {
        if (fileSaveTask != null && !fileSaveTask.isCancelled())
            fileSaveTask.cancel();
    }

    public static void addContainer(@NotNull final Player player, @NotNull final Location block) {
        ContainerCountStatistic countStatistic = new ContainerCountStatistic();
        PlayerContainersStatistic containersStatistic = new PlayerContainersStatistic();
        StatHandler.getStatistic(countStatistic);
        StatHandler.getStatistic(containersStatistic, player);
        countStatistic.increment();
        containersStatistic.add(block);
    }

    public static void removeContainer(@NotNull final Player player, @NotNull final Location block) {
        ContainerCountStatistic countStatistic = new ContainerCountStatistic();
        PlayerContainersStatistic containersStatistic = new PlayerContainersStatistic();
        StatHandler.getStatistic(countStatistic);
        StatHandler.getStatistic(containersStatistic, player);
        countStatistic.decrement();
        containersStatistic.remove(block);
    }

    public static void getStatistic(@NotNull Statistic<?, NBTCompound, Material> statistic) {
        getStatistic(statistic, null);
    }

    public static void getStatistic(@NotNull Statistic<?, NBTCompound, Material> statistic, @Nullable Player player) {
        switch (statistic.getType()) {
            case ALL:
            case PLAYER:
                // Only get the statistic if player is not null.
                if (player != null) {
                    final Optional<PlayerStatHandler> stats = getStatsForPlayer(player.getUniqueId().toString());
                    stats.ifPresent(handler -> handler.getStatistic(statistic));
                }

                // Let "ALL" fallthrough.
                if (statistic.getType() == StatisticType.PLAYER) break;
            case GLOBAL:
                final ServerStatHandler stats = getServerStats();
                stats.getStatistic(statistic);
                break;
        }
    }

    private static @NotNull Stream<PlayerStatHandler> getPlayerStats() {
        if (nbtFile == null) throw new RuntimeException("nbtFile was null.");
        if (!nbtFile.hasKey(PLAYER_SUB_KEY)) return Stream.empty();

        final NBTCompound list = nbtFile.getOrCreateCompound(PLAYER_SUB_KEY);
        return list
            .getKeys()
            .stream()
            .map((comp) -> new PlayerStatHandler(list.getCompound(comp)));
    }

    private static @NotNull Optional<PlayerStatHandler> getStatsForPlayer(@NotNull final String id) {
        Optional<PlayerStatHandler> ret = getPlayerStats()
            .filter((p) -> p.getName().equals(id))
            .findFirst();

        // Add the stats NBT to be sure that we always return a present optional value.
        if (!ret.isPresent()) {
            return Optional.of(StatHandler.addStatsForPlayer(id));
        }
        return ret;
    }

    private static @NotNull PlayerStatHandler addStatsForPlayer(@NotNull final String id) {
        if (nbtFile == null) throw new RuntimeException("nbtFile was null.");
        NBTCompound compound = nbtFile.getOrCreateCompound(PLAYER_SUB_KEY);
        compound.addCompound(id).setString("id", id);
        return new PlayerStatHandler(compound.getCompound(id));
    }

    private static @NotNull ServerStatHandler getServerStats() {
        if (nbtFile == null) throw new RuntimeException("nbtFile was null.");
        if (nbtFile.hasKey(SERVER_SUB_KEY) &&
            nbtFile.getType(SERVER_SUB_KEY) == NBTType.NBTTagCompound) {
            return new ServerStatHandler(nbtFile.getCompound(SERVER_SUB_KEY));
        } else {
            return new ServerStatHandler(nbtFile.addCompound(SERVER_SUB_KEY));
        }
    }
}
