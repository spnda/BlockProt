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
import de.sean.blockprot.bukkit.translation.TranslationKey;
import de.sean.blockprot.bukkit.translation.Translator;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class FriendSearchInventory {
    @NotNull
    private static final String inventoryName = Translator.get(TranslationKey.INVENTORIES__FRIENDS__SEARCH);

    public static void openAnvilInventory(@NotNull final Player requestingPlayer) {
        (new AnvilGUI.Builder())
            .onComplete(FriendSearchInventory::onCompleteCallback)
            .text("Name")
            .title(inventoryName)
            .plugin(BlockProt.getInstance())
            // .preventClose() // Allow the user to close
            .open(requestingPlayer);
    }

    private static AnvilGUI.Response onCompleteCallback(@NotNull final Player player, @NotNull final String searchQuery) {
        Inventory inventory = new FriendSearchResultScreen().fill(player, searchQuery);
        if (inventory == null) return AnvilGUI.Response.close();
        return AnvilGUI.Response.openInventory(inventory);
    }
}
