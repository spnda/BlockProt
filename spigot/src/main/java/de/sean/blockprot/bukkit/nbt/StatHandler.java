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
import de.sean.blockprot.bukkit.nbt.stats.BlockCountStatistic;
import de.sean.blockprot.bukkit.nbt.stats.BukkitStatistic;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import de.sean.blockprot.bukkit.tasks.StatisticFileSaveTask;
import de.sean.blockprot.nbt.stats.StatisticType;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * StatHandler for statistic related NBT data. Keeps track
 * of every statistic and periodically saves it to disk.
 * 
 * @since 1.0.0
 */
public final class StatHandler extends NBTHandler<NBTCompound> {
    static final String STAT_FILE_NAME = "blockprot_stats.nbt";
    static final String STAT_BACKUP_FILE_NAME = "blockprot_stats.nbt_old";

    static final String PLAYER_SUB_KEY = "player_stats";
    static final String SERVER_SUB_KEY = "server_stats";

    private static @Nullable BukkitTask fileSaveTask;

    private static @Nullable File baseFile;
    private static @Nullable File backupFile;
    private static @Nullable File temporarySwapFile;

    private static @Nullable NBTFile nbtFile;
    private static @Nullable NBTFile backupNbtFile;

    /** Internal constructor to copy the NBT compound. */
    private StatHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
    }

    /** Update the NBT compound of given statistic. */
    public void updateStatistic(final @NotNull BukkitStatistic<?> statistic) {
        statistic.updateContainer(this.container);
    }

    public static void enable() {
        if (nbtFile != null) return;
        try {
            // If there's no worlds on this server, that's not our issue.
            World world = Bukkit.getServer().getWorlds().get(0);

            baseFile          = new File(world.getWorldFolder(), STAT_FILE_NAME);
            backupFile        = new File(world.getWorldFolder(), STAT_BACKUP_FILE_NAME);
            temporarySwapFile = new File(world.getWorldFolder(), STAT_FILE_NAME + ".tmp");

            try {
                new NBTFile(baseFile);
            } catch (NbtApiException e) {
                /* The base NBT file has been corrupted. */
                baseFile.delete();
            }

            if (!baseFile.exists())
                saveFile();

            nbtFile = new NBTFile(baseFile);
            backupNbtFile = new NBTFile(backupFile);

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
        /* We will always first save to a temporary file and then
           swap it with the actual file. This ensures that the write
           process on the actual file does not get interrupted, thus
           never corrupting it. */
        if (baseFile != null && backupFile != null && temporarySwapFile != null) {
            if (backupNbtFile != null)
                backupNbtFile.save();

            /* Check if the backup file saved correctly */
            try {
                if (backupFile.exists())
                    new NBTFile(backupFile);
            } catch (NbtApiException e) {
                BlockProt.getInstance().getLogger().warning("Failed to re-read statistic NBT File!");
                backupFile.delete();
                return;
            }

            if (baseFile.exists()) {
                if (!backupFile.renameTo(temporarySwapFile)) {
                    throw new IOException("Failed to rename backup NBT file.");
                }

                if (!baseFile.renameTo(backupFile)) {
                    throw new IOException("Failed to rename base file to backup file.");
                }

                if (!temporarySwapFile.renameTo(baseFile)) {
                    throw new IOException("Failed to rename temporary file to base file.");
                }
            } else {
                /* The base NBT file doesn't exist, probably got corrupted */
                if (!backupFile.renameTo(baseFile)) {
                    throw new IOException("Failed to rename backup NBT file.");
                }
            }
        }
    }

    public static void disable() {
        if (fileSaveTask != null && !fileSaveTask.isCancelled())
            fileSaveTask.cancel();

        try {
            StatHandler.saveFile();
        } catch (IOException ignored) {}
    }

    /**
     * Adds given block to given player's block statistic, while also incrementing the
     * global block count.
     */
    public static void addBlock(@NotNull final Player player, @NotNull final Location block) {
        BlockCountStatistic countStatistic = new BlockCountStatistic();
        PlayerBlocksStatistic containersStatistic = new PlayerBlocksStatistic();
        StatHandler.getStatistic(countStatistic);
        StatHandler.getStatistic(containersStatistic, player);
        countStatistic.increment();
        containersStatistic.add(block);
    }

    /**
     * Removes given block from the player's statistic, while also decrementing
     * the global block count.
     */
    public static void removeContainer(@NotNull final Player player, @NotNull final Location block) {
        BlockCountStatistic countStatistic = new BlockCountStatistic();
        PlayerBlocksStatistic containersStatistic = new PlayerBlocksStatistic();
        StatHandler.getStatistic(countStatistic);
        StatHandler.getStatistic(containersStatistic, player);
        countStatistic.decrement();
        containersStatistic.remove(block);
    }

    /**
     * Get a statistic. To query player specific statistics please
     * use {@link #getStatistic(BukkitStatistic, Player)}.
     */
    public static void getStatistic(@NotNull BukkitStatistic<?> statistic) {
        if (statistic.getType() == StatisticType.PLAYER) {
            throw new RuntimeException("StatHandler#getStatistic with player statistic called without player");
        }
        getStatistic(statistic, null);
    }

    public static void getStatistic(@NotNull BukkitStatistic<?> statistic, @Nullable Player player) {
        switch (statistic.getType()) {
            case ALL:
            case PLAYER:
                // Only get the statistic if player is not null.
                if (player != null) {
                    final Optional<StatHandler> stats = getStatsForPlayer(player.getUniqueId().toString());
                    stats.ifPresent(handler -> handler.updateStatistic(statistic));
                }

                // Let "ALL" fallthrough.
                if (statistic.getType() == StatisticType.PLAYER) break;
            case GLOBAL:
                final StatHandler stats = getServerStats();
                stats.updateStatistic(statistic);
                break;
        }
    }

    private static @NotNull Stream<StatHandler> getPlayerStats() {
        if (nbtFile == null) throw new RuntimeException("nbtFile was null.");
        if (!nbtFile.hasKey(PLAYER_SUB_KEY)) return Stream.empty();

        final NBTCompound list = nbtFile.getOrCreateCompound(PLAYER_SUB_KEY);
        return list
            .getKeys()
            .stream()
            .map((comp) -> new StatHandler(list.getCompound(comp)));
    }

    private static @NotNull Optional<StatHandler> getStatsForPlayer(@NotNull final String id) {
        Optional<StatHandler> ret = getPlayerStats()
            .filter((p) -> p.getName().equals(id))
            .findFirst();

        // Add the stats NBT to be sure that we always return a present optional value.
        if (!ret.isPresent()) {
            return Optional.of(StatHandler.addStatsForPlayer(id));
        }
        return ret;
    }

    private static @NotNull StatHandler addStatsForPlayer(@NotNull final String id) {
        if (nbtFile == null) throw new RuntimeException("nbtFile was null.");
        NBTCompound compound = nbtFile.getOrCreateCompound(PLAYER_SUB_KEY);
        compound.addCompound(id).setString("id", id);
        return new StatHandler(compound.getCompound(id));
    }

    private static @NotNull StatHandler getServerStats() {
        if (nbtFile == null) throw new RuntimeException("nbtFile was null.");
        if (nbtFile.hasKey(SERVER_SUB_KEY) &&
            nbtFile.getType(SERVER_SUB_KEY) == NBTType.NBTTagCompound) {
            return new StatHandler(nbtFile.getCompound(SERVER_SUB_KEY));
        } else {
            return new StatHandler(nbtFile.addCompound(SERVER_SUB_KEY));
        }
    }
}
