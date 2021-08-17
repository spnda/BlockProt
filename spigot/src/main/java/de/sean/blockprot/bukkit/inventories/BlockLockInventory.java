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
import de.sean.blockprot.bukkit.events.BlockAccessEditMenuEvent;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockLockInventory extends BlockProtInventory {
    private boolean redstone = false;

    @Override
    int getSize() {
        return InventoryConstants.singleLine;
    }

    @NotNull
    @Override
    String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__BLOCK_LOCK);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        Block block = state.getBlock();
        if (block == null) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        Player player = (Player) event.getWhoClicked();
        if (BlockProt.getDefaultConfig().isLockable(block.getType()) && event.getSlot() == 0) {
            applyChanges(
                player,
                (handler) -> handler.lockBlock(player),
                null
            );
            closeAndOpen(player, null);
        } else if (item.getType() == Material.REDSTONE || item.getType() == Material.GUNPOWDER) {
            redstone = !redstone;
            setItemStack(
                1,
                redstone ? Material.REDSTONE : Material.GUNPOWDER,
                redstone ? TranslationKey.INVENTORIES__REDSTONE__DISALLOW : TranslationKey.INVENTORIES__REDSTONE__ALLOW
            );
        } else if (item.getType() == Material.PLAYER_HEAD) {
            closeAndOpen(
                player,
                new FriendManageInventory().fill(player)
            );
        } else if (item.getType() == Material.OAK_SIGN) {
            BlockNBTHandler handler = getNbtHandlerOrNull(block);
            closeAndOpen(
                player,
                handler == null
                    ? null
                    : new BlockInfoInventory().fill(player, handler)
            );
        } else {
            closeAndOpen(
                player,
                null
            );
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.getBlock() != null) {
            applyChanges(
                (Player) event.getPlayer(),
                (handler) -> handler.lockRedstoneForBlock(
                    event.getPlayer().getUniqueId().toString(),
                    redstone
                ),
                null
            );
        }
    }

    public Inventory fill(Player player, Material material, BlockNBTHandler handler) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        String playerUuid = player.getUniqueId().toString();
        String owner = handler.getOwner();
        redstone = handler.getRedstone();

        if (owner.isEmpty()) {
            setItemStack(
                0,
                getProperMaterial(material),
                TranslationKey.INVENTORIES__LOCK
            );
        } else if (owner.equals(playerUuid) || state.menuAccess == BlockAccessEditMenuEvent.MenuAccess.ADMIN) {
            setItemStack(
                0,
                getProperMaterial(material),
                TranslationKey.INVENTORIES__UNLOCK
            );
        }

        if (owner.equals(playerUuid) && state.menuAccess.ordinal() >= BlockAccessEditMenuEvent.MenuAccess.NORMAL.ordinal()) {
            setItemStack(
                1,
                (redstone) ? Material.REDSTONE : Material.GUNPOWDER,
                (redstone) ? TranslationKey.INVENTORIES__REDSTONE__DISALLOW : TranslationKey.INVENTORIES__REDSTONE__ALLOW
            );
            setItemStack(
                2,
                Material.PLAYER_HEAD,
                TranslationKey.INVENTORIES__FRIENDS__MANAGE
            );
        }

        if (!owner.isEmpty() &&
            state.menuAccess.ordinal() >= BlockAccessEditMenuEvent.MenuAccess.INFO.ordinal()
        ) {
            setItemStack(
                InventoryConstants.lineLength - 2,
                Material.OAK_SIGN,
                TranslationKey.INVENTORIES__BLOCK_INFO
            );
        }
        setBackButton();
        return inventory;
    }
}
