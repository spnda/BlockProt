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

package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.VersionCompat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class FriendSearchInventory {
    public static void openChatInput(@NotNull final Player requestingPlayer) {
        if (VersionCompat.isPaper()) {
            // Paper 1.19+ — native chat input with Tab auto-complete.
            ChatInput.open(
                requestingPlayer,
                BlockProt.getInstance(),
                text -> handleResult(requestingPlayer, text)
            );
        } else {
            // Spigot fallback — server-side anvil GUI.
            String prompt = Translator.get(TranslationKey.INVENTORIES__FRIENDS__SEARCH);
            AnvilInput.open(
                requestingPlayer,
                BlockProt.getInstance(),
                prompt,
                prompt,
                text -> handleResult(requestingPlayer, text)
            );
        }
    }

    private static void handleResult(@NotNull Player player, String text) {
        if (text == null || text.isBlank()) return;
        Inventory inventory = new FriendSearchResultInventory().fill(player, text);
        if (inventory != null) {
            player.openInventory(inventory);
        }
    }
}
