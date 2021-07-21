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
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BlockInfoInventory extends BlockProtInventory {
    @Override
    int getSize() {
        return InventoryConstants.tripleLine;
    }

    @NotNull
    @Override
    String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__BLOCK_INFO);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE:
                if (state.getBlock() == null) break;
                BlockNBTHandler handler = new BlockNBTHandler(state.getBlock());
                closeAndOpen(
                    player,
                    new BlockLockInventory().fill(player, state.getBlock().getType(), handler)
                );
                break;
            case CYAN_STAINED_GLASS_PANE:
                if (state.getBlock() == null) break;
                if (state.friendPage >= 1) {
                    state.friendPage--;
                    closeAndOpen(
                        player,
                        this.fill(player, new BlockNBTHandler(state.getBlock()))
                    );
                }
                break;
            case BLUE_STAINED_GLASS_PANE:
                if (state.getBlock() == null) break;
                final ItemStack lastFriendInInventory = inventory.getItem(InventoryConstants.tripleLine - 1);
                if (lastFriendInInventory != null && lastFriendInInventory.getAmount() != 0) {
                    // There's an item in the last slot => The page is fully filled up, meaning we should go to the next page.
                    state.friendPage++;
                    closeAndOpen(
                        player,
                        this.fill(player, new BlockNBTHandler(state.getBlock()))
                    );
                }
                break;
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    @NotNull
    public Inventory fill(Player player, BlockNBTHandler handler) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        String owner = handler.getOwner();
        List<FriendHandler> friends = handler.getFriends();
        boolean redstone = handler.getRedstone();

        inventory.clear();
        state.friendResultCache.clear();
        int max = Math.min(friends.size() - 1, InventoryConstants.doubleLine); // Maximum of 2 lines of skulls
        for (int i = 0; i < max; i++) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(friends.get(i).getName()));
            this.setItemStack(InventoryConstants.lineLength + i, Material.SKELETON_SKULL, offlinePlayer.getName());
            state.friendResultCache.add(offlinePlayer);
        }

        if (!owner.isEmpty()) {
            setPlayerSkull(0, Bukkit.getOfflinePlayer(UUID.fromString(owner)));
        }
        if (state.friendPage == 0 && friends.size() >= InventoryConstants.doubleLine) {
            setItemStack(
                InventoryConstants.lineLength - 3,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE
            );
            setItemStack(
                InventoryConstants.lineLength - 2,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE
            );
        }
        setItemStack(
            1,
            redstone ? Material.REDSTONE : Material.GUNPOWDER,
            redstone ? TranslationKey.INVENTORIES__REDSTONE__ALLOWED : TranslationKey.INVENTORIES__REDSTONE__DISALLOWED
        );
        setBackButton(InventoryConstants.lineLength - 1);

        Bukkit.getScheduler().runTaskAsynchronously(
            BlockProt.getInstance(),
            () -> {
                int i = 0;
                while (i < InventoryConstants.doubleLine && i < state.friendResultCache.size()) {
                    setPlayerSkull(InventoryConstants.lineLength + i, state.friendResultCache.get(i));
                    i++;
                }
            }
        );

        return inventory;
    }
}
