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

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.nbt.stats.BukkitListStatistic;
import de.sean.blockprot.nbt.stats.ListStatisticItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class StatisticListInventory extends BlockProtInventory {
    /** Stores the statistic for pages to use. */
    private BukkitListStatistic<ListStatisticItem<?, Material>, ?> statistic;

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
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case CYAN_STAINED_GLASS_PANE:
                if (state.currentPageIndex >= 1) {
                    state.currentPageIndex--;
                    closeAndOpen(
                        event.getWhoClicked(),
                        this.fill((Player) event.getWhoClicked(), null)
                    );
                }
                break;
            case BLUE_STAINED_GLASS_PANE:
                state.currentPageIndex++;
                closeAndOpen(event.getWhoClicked(), fill((Player) event.getWhoClicked(), null));
                break;
            case BLACK_STAINED_GLASS_PANE:
                closeAndOpen(event.getWhoClicked(), new StatisticsInventory().fill((Player) event.getWhoClicked()));
                break;
            default:
                // Ignore clicks on statistic items.
                break;
        }
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    /**
     * @param stat Can be null to indicate that this inventory should use the previously
     *             cached statistic. Will throw {@link RuntimeException} if there is no
     *             cached statistic.
     */
    public Inventory fill(@NotNull final Player player, @Nullable final BukkitListStatistic<ListStatisticItem<?, Material>, ?> stat)
        throws RuntimeException {
        if (stat != null) this.statistic = stat;
        if (this.statistic == null) throw new RuntimeException("No cached statistic available.");

        List<ListStatisticItem<?, Material>> list = this.statistic.get();
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        // Verify that all block locations are valid and remove them if necessary.
        // We'll log these into the console for users to report so that this doesn't
        // need to happen.
        list = list.stream()
                .filter((item) -> BlockProt.getDefaultConfig().isLockable(item.getItemType()))
                .collect(Collectors.toList());

        final int max = this.getSize() - 3;
        int offset = max * state.currentPageIndex;
        for (int i = 0; i < Math.min(list.size() - offset, max); ++i) {
            final ListStatisticItem<?, Material> item = list.get(offset + i);
            setItemStack(
                i,
                item.getItemType(),
                item.getTitle()
            );
        }

        if (list.size() - offset > max) {
            setItemStack(
                max,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE);
            setItemStack(
                max + 1,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE);
        }
        setBackButton();
        return inventory;
    }
}
