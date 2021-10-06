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
import de.sean.blockprot.bukkit.nbt.stats.BlockCountStatistic;
import de.sean.blockprot.bukkit.nbt.stats.BukkitListStatistic;
import de.sean.blockprot.bukkit.nbt.stats.BukkitStatistic;
import de.sean.blockprot.bukkit.nbt.stats.PlayerBlocksStatistic;
import de.sean.blockprot.nbt.stats.StatisticOnClickAction;
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
        return InventoryConstants.sextupletLine;
    }

    @Override
    @NotNull String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__STATISTICS__STATISTICS);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLUE_STAINED_GLASS_PANE:
                if (event.getWhoClicked() instanceof Player) {
                    state.currentPageIndex ^= 1L; // Toggles the first bit, switching between 0 and 1.
                    this.fill((Player) event.getWhoClicked());
                }
                break;
            case BLACK_STAINED_GLASS_PANE:
                closeAndOpen(event.getWhoClicked(), null);
                break;
            default:
                BukkitStatistic<?> stat = state.currentPageIndex == 0
                    ? playerStatistics.get(event.getSlot())
                    : serverStatistics.get(event.getSlot());
                openStatInventory(stat, (Player) event.getWhoClicked());
                break;
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    public void openStatInventory(@NotNull final BukkitStatistic<?> stat, @NotNull final Player player) {
        if (stat.getClickAction() == StatisticOnClickAction.NONE) return;
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof StatisticListInventory) return;
        if (stat.getClickAction() == StatisticOnClickAction.LIST_MENU && stat instanceof BukkitListStatistic) {
            closeAndOpen(player, new StatisticListInventory().fill(player, (BukkitListStatistic) stat));
        }
    }

    public Inventory fill(@NotNull final Player player) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null || state.currentPageIndex >= 2) return inventory; // We only have 2 pages.

        final List<BukkitStatistic<?>> statistics = state.currentPageIndex == 0
            ? playerStatistics
            : serverStatistics;
        for (int i = 0; i < statistics.size() && i < getSize() - 2; ++i) {
            BukkitStatistic<?> stat = statistics.get(i);
            StatHandler.getStatistic(stat, player);
            setItemStack(
                i,
                stat.getItemType(),
                stat.getTitle()
            );
        }

        setItemStack(
            getSize() - 2,
            Material.BLUE_STAINED_GLASS_PANE,
            state.currentPageIndex == 0
                ? TranslationKey.INVENTORIES__STATISTICS__GLOBAL_STATISTICS
                : TranslationKey.INVENTORIES__STATISTICS__PLAYER_STATISTICS
        );
        setBackButton();
        return inventory;
    }
}
