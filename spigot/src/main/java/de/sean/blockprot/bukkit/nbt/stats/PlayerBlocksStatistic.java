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

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.nbt.stats.StatisticType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public final class PlayerBlocksStatistic extends LocationListStatistic {
    @Override
    public @NotNull String getKey() {
        return "containers";
    }

    @Override
    public @NotNull StatisticType getType() {
        return StatisticType.PLAYER;
    }

    @Override
    public @NotNull Material getItemType() {
        return Material.CHEST;
    }

    @Override
    public String getTitle() {
        return Translator.get(TranslationKey.INVENTORIES__STATISTICS__PLAYER_BLOCKS);
    }
}
