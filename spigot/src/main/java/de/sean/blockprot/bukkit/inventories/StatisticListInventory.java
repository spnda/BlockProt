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
import de.sean.blockprot.bukkit.nbt.stats.BukkitListStatistic;
import de.sean.blockprot.nbt.stats.ListStatisticItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class StatisticListInventory extends BlockProtInventory {
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
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE && event.getWhoClicked() instanceof Player) {
            closeAndOpen((Player) event.getWhoClicked(), new StatisticsInventory().fill((Player) event.getWhoClicked()));
        }
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    public Inventory fill(@NotNull final Player player, @NotNull final BukkitListStatistic<ListStatisticItem<?, Material>, ?> stat) {
        List<ListStatisticItem<?, Material>> list = stat.get();

        for (int i = 0; i < list.size() && i < (InventoryConstants.tripleLine - 2); ++i) {
            setItemStack(i, list.get(i).getItemType(), list.get(i).getTitle());
        }

        setBackButton();
        return inventory;
    }
}
