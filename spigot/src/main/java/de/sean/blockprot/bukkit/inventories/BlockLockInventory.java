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
import de.sean.blockprot.bukkit.events.BlockAccessMenuEvent;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockLockInventory extends BlockProtInventory {
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
            BlockNBTHandler handler = getNbtHandlerOrNull(block);
            closeAndOpen(
                player,
                handler == null
                    ? null
                    : new RedstoneSettingsInventory().fill(player, state)
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
    }

    public Inventory fill(Player player, Material material, BlockNBTHandler handler) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        String owner = handler.getOwner();

        Bukkit.getLogger().info(state.menuPermissions.toString());
        if (state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.LOCK)) {
            setItemStack(
                0,
                getProperMaterial(material),
                owner.isEmpty()
                    ? TranslationKey.INVENTORIES__LOCK
                    : TranslationKey.INVENTORIES__UNLOCK
            );
        }

        if (state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.MANAGER)) {
            setItemStack(
                1,
                Material.REDSTONE,
                TranslationKey.INVENTORIES__REDSTONE__SETTINGS
            );
            setItemStack(
                2,
                Material.PLAYER_HEAD,
                TranslationKey.INVENTORIES__FRIENDS__MANAGE
            );
        }

        if (!owner.isEmpty() && state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.INFO)) {
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
