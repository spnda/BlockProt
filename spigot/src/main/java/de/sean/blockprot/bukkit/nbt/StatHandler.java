/*
 * Copyright (C) 2021 - 2025 spnda
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
import de.sean.blockprot.bukkit.util.BlockUtil;
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
import java.io.OutputStream;
import java.nio.file.Files;
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

    private static @Nullable File backupFile;

    private static @Nullable File temporarySwapFile;

    private static @Nullable NBTFile nbtFile;

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

            final File baseFile = new File(world.getWorldFolder(), STAT_FILE_NAME);
            backupFile = new File(world.getWorldFolder(), STAT_BACKUP_FILE_NAME);
            temporarySwapFile = new File(world.getWorldFolder(), STAT_FILE_NAME + ".tmp");

            if (!backupFile.exists())
                if (!backupFile.createNewFile())
                    throw new IOException("Failed to create backup statistic NBT file!");

            try {
                // The NBTFile will create the file if it doesn't exist.
                nbtFile = new NBTFile(baseFile);
            } catch (NbtApiException e) {
                // The base file has been corrupted and couldn't be loaded.
                if (backupFile.exists()) {
                    try {
                        // A backup exists, we'll try reading from it. If this fails, then that's too bad.
                        final NBTFile backupNbtFile = new NBTFile(backupFile);
                        backupNbtFile.writeCompound(Files.newOutputStream(baseFile.toPath()));
                        nbtFile = new NBTFile(baseFile);
                    } catch (NbtApiException be) {
                        // We have no backup, or it is also corrupt, we need to just delete and start from the beginning.
                        BlockProt.getInstance().getLogger().warning("The statistics file and its backup are corrupted!");
                        backupFile.delete();
                        baseFile.delete();
                        nbtFile = new NBTFile(baseFile);
                    }
                } else {
                    // We have no backup, we need to just delete and start from the beginning.
                    BlockProt.getInstance().getLogger().warning("The statistics file was corrupted and no backup file was found!");
                    baseFile.delete();
                    nbtFile = new NBTFile(baseFile);
                }
            }

            fileSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                BlockProt.getInstance(),
                new StatisticFileSaveTask(),
                0L,
                5 * 60 * 20 // 5 minutes * 60 seconds * 20 ticks
            );
        } catch (Throwable e) {
            BlockProt.getInstance().getLogger().warning("Failed to open BlockProt statistic file.");
        }
    }

    public static void saveFile() throws IOException {
        /* We will always first save to a temporary file and then
           swap it with the actual file. This ensures that the write
           process on the actual file does not get interrupted, thus
           never corrupting it. */
        if (nbtFile != null && backupFile != null && temporarySwapFile != null) {
            OutputStream outputStream = Files.newOutputStream(backupFile.toPath());
            nbtFile.writeCompound(outputStream);
            outputStream.close();

            try {
                if (backupFile.exists())
                    new NBTFile(backupFile);
            } catch (NbtApiException e) {
                BlockProt.getInstance().getLogger().warning("Failed to re-read statistic NBT File!");
                backupFile.delete();
                return;
            }

            if (nbtFile.getFile().exists() && backupFile.exists()) {
                try {
                    // TODO: This sometimes makes temporarySwapFile disappear...
                    Files.move(backupFile.toPath(), temporarySwapFile.toPath());
                    Files.move(nbtFile.getFile().toPath(), backupFile.toPath());
                    Files.move(temporarySwapFile.toPath(), nbtFile.getFile().toPath());
                } catch (IOException e) {
                    throw new IOException("Failed to swap backup NBT file: " + e.getMessage());
                }
            } else if (backupFile.exists()) {
                /* The base NBT file doesn't exist, probably got corrupted */
                if (!backupFile.renameTo(nbtFile.getFile())) {
                    throw new IOException("Failed to rename backup NBT file.");
                }
            } else {
                // This shouldn't happen
                throw new IOException("NBT files have been deleted after usage!");
            }
        }
    }

    public static void disable() {
        if (fileSaveTask != null && !fileSaveTask.isCancelled())
            fileSaveTask.cancel();

        try {
            StatHandler.saveFile();
        } catch (IOException e) {
            BlockProt.getInstance().getLogger().warning("Failed to save statistics file: " + e.getMessage());
        }
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
    @Deprecated
    public static void removeContainer(@NotNull final Player player, @NotNull final Location block) {
        BlockCountStatistic countStatistic = new BlockCountStatistic();
        PlayerBlocksStatistic containersStatistic = new PlayerBlocksStatistic();
        StatHandler.getStatistic(countStatistic);
        StatHandler.getStatistic(containersStatistic, player);
        countStatistic.decrement();
        containersStatistic.remove(block);
    }

    /**
     * Removes given block from the player's statistic, while also decrementing
     * the global block count. Also takes care of special blocks, like doors.
     * @since 1.0.3
     */
    public static void removeContainer(@NotNull final Player player, @NotNull final Block block) {
        /* Remove the other half of the door from the statistics as well */
        if (BlockProt.getDefaultConfig().isLockableDoor(block.getType())) {
            final Block otherDoor = BlockUtil.getOtherDoorHalf(block.getState());
            if (otherDoor != null)
                StatHandler.removeContainer(player, otherDoor.getLocation());
        }

        BlockCountStatistic countStatistic = new BlockCountStatistic();
        PlayerBlocksStatistic containersStatistic = new PlayerBlocksStatistic();
        StatHandler.getStatistic(countStatistic);
        StatHandler.getStatistic(containersStatistic, player);
        countStatistic.decrement();
        containersStatistic.remove(block.getLocation());
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
        if (ret.isEmpty()) {
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
