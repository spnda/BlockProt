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

package de.sean.blockprot.bukkit.nbt.stats;

import de.sean.blockprot.bukkit.nbt.NBTHandler;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * StatHandler for statistic related NBT data.
 * NOTE: DO NOT USE THIS DIRECTLY, see {@link de.sean.blockprot.bukkit.StatisticManager}.
 */
public final class StatHandler extends NBTHandler<NBTFile> {
    static final String STAT_FILE_NAME = "blockprot_stats.nbt";
    static final String PLAYER_SUB_KEY = "player_stats";
    static final String SERVER_SUB_KEY = "server_stats";

    private @NotNull final File file;

    public StatHandler() throws IOException {
        super();
        // If there's no worlds on this server, that's not our issue.
        World world = Bukkit.getServer().getWorlds().get(0);
        this.file = new File(world.getWorldFolder(), STAT_FILE_NAME);
        this.container = new NBTFile(this.file);
    }

    public @NotNull Stream<PlayerStatHandler> getPlayerStats() {
        if (!this.container.hasKey(PLAYER_SUB_KEY)) return Stream.empty();

        final NBTCompound list = this.container.getOrCreateCompound(PLAYER_SUB_KEY);
        return list
            .getKeys()
            .stream()
            .map((comp) -> new PlayerStatHandler(list.getCompound(comp)));
    }

    public @NotNull Optional<PlayerStatHandler> getStatsForPlayer(@NotNull final String id) {
        return getPlayerStats()
            .filter((p) -> p.getName().equals(id))
            .findFirst();
    }

    public @NotNull PlayerStatHandler addStatsForPlayer(@NotNull final String id) {
        NBTCompound compound = container.getOrCreateCompound(PLAYER_SUB_KEY);
        compound.addCompound(id).setString("id", id);
        return new PlayerStatHandler(compound.getCompound(id));
    }

    public @NotNull ServerStatHandler getServerStats() {
        if (container.hasKey(SERVER_SUB_KEY) &&
            container.getType(SERVER_SUB_KEY) == NBTType.NBTTagCompound) {
            return new ServerStatHandler(container.getCompound(SERVER_SUB_KEY));
        } else {
            return new ServerStatHandler(container.addCompound(SERVER_SUB_KEY));
        }
    }

    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
        if (handler instanceof StatHandler) {
            StatHandler statHandler = (StatHandler) handler;
            // this.getPlayerStats().mergeHandler(statHandler.getPlayerStats());
            this.getServerStats().mergeHandler(statHandler.getServerStats());
        }
    }
}
