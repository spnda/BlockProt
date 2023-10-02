/*
 * Copyright (C) 2021 - 2023 spnda
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
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FriendSearchInventory {
    public static void openAnvilInventory(@NotNull final Player requestingPlayer) {
        (new AnvilGUI.Builder())
            .onClick(FriendSearchInventory::onCompleteCallback)
            .text("Name")
            .title(Translator.get(TranslationKey.INVENTORIES__FRIENDS__SEARCH))
            .plugin(BlockProt.getInstance())
            .onClose(FriendSearchInventory::onCloseCallback)
            .open(requestingPlayer);
    }

    private static @NotNull @Unmodifiable List<AnvilGUI.ResponseAction> onCompleteCallback(@NotNull final Integer slot, @NotNull AnvilGUI.StateSnapshot snapshot) {
        if (slot != AnvilGUI.Slot.OUTPUT) {
            return Collections.emptyList();
        }

        Inventory inventory = new FriendSearchResultInventory().fill(snapshot.getPlayer(), snapshot.getText());
        if (inventory == null) return List.of(AnvilGUI.ResponseAction.close());
        return List.of(AnvilGUI.ResponseAction.openInventory(inventory));
    }

    private static void onCloseCallback(@NotNull AnvilGUI.StateSnapshot snapshot) {
        InventoryState.remove(snapshot.getPlayer().getUniqueId());
    }
}
