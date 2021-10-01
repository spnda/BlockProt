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

package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.StatHandler;
import de.sean.blockprot.bukkit.nbt.stats.ContainerCountStatistic;
import de.sean.blockprot.bukkit.nbt.stats.PlayerContainersStatistic;
import de.sean.blockprot.nbt.stats.ListStatistic;
import de.sean.blockprot.nbt.stats.OnClickAction;
import de.sean.blockprot.nbt.stats.Statistic;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class StatisticsInventory extends BlockProtInventory {
    private final List<Statistic<?, NBTCompound, Material>> playerStatistics = new ArrayList<Statistic<?, NBTCompound, Material>>() {{
        add(new PlayerContainersStatistic());
    }};
    private final List<Statistic<?, NBTCompound, Material>> serverStatistics = new ArrayList<Statistic<?, NBTCompound, Material>>() {{
        add(new ContainerCountStatistic());
    }};

    @Override
    int getSize() {
        return InventoryConstants.tripleLine;
    }

    @Override
    @NotNull String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__STATISTICS);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            Statistic<?, NBTCompound, Material> stat;
            if (event.getSlot() < InventoryConstants.singleLine) {
                // Player stat
                stat = playerStatistics.get(event.getSlot());
            } else {
                // Server stat
                stat = serverStatistics.get(event.getSlot() - InventoryConstants.singleLine);
            }
            openStatInventory(stat, (Player) event.getWhoClicked());
        } else {
            closeAndOpen((Player) event.getWhoClicked(), null);
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    public void openStatInventory(@NotNull final Statistic<?, NBTCompound, Material> stat, @NotNull final Player player) {
        if (stat.getClickAction() == OnClickAction.NONE) return;
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof StatisticListInventory) return;
        if (stat.getClickAction() == OnClickAction.LIST_MENU && stat instanceof ListStatistic) {
            closeAndOpen(player, new StatisticListInventory().fill(player, (ListStatistic) stat));
        }
    }

    public Inventory fill(@NotNull final Player player) {
        for (int i = 0; i < playerStatistics.size() && i < InventoryConstants.singleLine; i++) {
            Statistic<?, NBTCompound, Material> stat = playerStatistics.get(i);
            StatHandler.getStatistic(stat, player);
            setItemStack(
                i,
                stat.getItemType(),
                stat.toString()
            );
        }

        for (int i = 0; i < serverStatistics.size() && i < InventoryConstants.singleLine; i++) {
            Statistic<?, NBTCompound, Material> stat = serverStatistics.get(i);
            StatHandler.getStatistic(stat);
            setItemStack(
                InventoryConstants.singleLine + i,
                stat.getItemType(),
                stat.toString()
            );
        }

        setBackButton();
        return inventory;
    }
}
