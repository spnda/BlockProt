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
import de.sean.blockprot.bukkit.nbt.stats.BukkitListStatistic;
import de.sean.blockprot.bukkit.nbt.stats.BukkitStatistic;
import de.sean.blockprot.bukkit.nbt.stats.BlockCountStatistic;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import de.sean.blockprot.nbt.stats.OnClickAction;
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
    private final List<BukkitStatistic<?>> playerStatistics = new ArrayList<BukkitStatistic<?>>() {{
        add(new PlayerBlocksStatistic());
    }};
    private final List<BukkitStatistic<?>> serverStatistics = new ArrayList<BukkitStatistic<?>>() {{
        add(new BlockCountStatistic());
    }};

    @Override
    int getSize() {
        return InventoryConstants.tripleLine;
    }

    @Override
    @NotNull String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__STATISTICS__STATISTICS);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            BukkitStatistic<?> stat;
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

    public void openStatInventory(@NotNull final BukkitStatistic<?> stat, @NotNull final Player player) {
        if (stat.getClickAction() == OnClickAction.NONE) return;
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof StatisticListInventory) return;
        if (stat.getClickAction() == OnClickAction.LIST_MENU && stat instanceof BukkitListStatistic) {
            closeAndOpen(player, new StatisticListInventory().fill(player, (BukkitListStatistic) stat));
        }
    }

    public Inventory fill(@NotNull final Player player) {
        for (int i = 0; i < playerStatistics.size() && i < InventoryConstants.singleLine; i++) {
            BukkitStatistic<?> stat = playerStatistics.get(i);
            StatHandler.getStatistic(stat, player);
            setItemStack(
                i,
                stat.getItemType(),
                stat.getTitle()
            );
        }

        for (int i = 0; i < serverStatistics.size() && i < InventoryConstants.singleLine; i++) {
            BukkitStatistic<?> stat = serverStatistics.get(i);
            StatHandler.getStatistic(stat);
            setItemStack(
                InventoryConstants.singleLine + i,
                stat.getItemType(),
                stat.getTitle()
            );
        }

        setBackButton();
        return inventory;
    }
}
