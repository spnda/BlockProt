/*
 * Copyright (C) 2021 - 2022 spnda
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
import de.sean.blockprot.bukkit.nbt.PlayerInventoryClipboard;
import net.wesjd.anvilgui.AnvilGUI;
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
        return InventoryConstants.doubleLine;
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
        } else if (item.getType() == Material.REDSTONE) {
            BlockNBTHandler handler = getNbtHandlerOrNull(block);
            closeAndOpen(
                player,
                handler == null
                    ? null
                    : new RedstoneSettingsInventory().fill(player, state)
            );
        } else {
            switch (item.getType()) {
                case PLAYER_HEAD -> closeAndOpen(
                    player,
                    new FriendManageInventory().fill(player)
                );
                case OAK_SIGN -> {
                    var handler = getNbtHandlerOrNull(block);
                    closeAndOpen(
                        player,
                        handler == null
                            ? null
                            : new BlockInfoInventory().fill(player, handler)
                    );
                }
                case KNOWLEDGE_BOOK -> {
                    // Paste
                    var handler = getNbtHandlerOrNull(block);
                    var container = PlayerInventoryClipboard.get(player.getUniqueId().toString());
                    if (handler != null && container != null)
                        handler.pasteNbt(container);
                }
                case PAPER -> {
                    // Copy
                    var handler = getNbtHandlerOrNull(block);
                    if (handler != null) {
                        PlayerInventoryClipboard.set(player.getUniqueId().toString(), handler.getNbtCopy());
                        // The player probably doesn't want to paste the data onto the same container.
                        closeAndOpen(player, null);
                    }
                }
                case NAME_TAG -> {
                    player.closeInventory();
                    new AnvilGUI.Builder()
                        .text("Block name")
                        .title(Translator.get(TranslationKey.INVENTORIES__SET_BLOCK_NAME))
                        .plugin(BlockProt.getInstance())
                        .onComplete((Player kPlayer, String name) -> {
                            var invState = InventoryState.get(kPlayer.getUniqueId());
                            assert(invState.getBlock() != null);

                            new BlockNBTHandler(invState.getBlock()).setName(name);
                            Inventory inventory = new BlockLockInventory().fill(player, block.getType(), new BlockNBTHandler(block));
                            if (inventory == null) return AnvilGUI.Response.close();
                            return AnvilGUI.Response.openInventory(inventory);
                        })
                        .open(player);
                }
                default -> closeAndOpen(
                    player,
                    null
                );
            }
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
    }

    public Inventory fill(Player player, Material material, BlockNBTHandler handler) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        var isNotProtected = handler.isNotProtected();
        // This means the user only has INFO permissions but the block is not locked and can
        // therefore not provide any information.
        if (isNotProtected && state.menuPermissions.size() == 1
            && state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.INFO))
            return null;

        if (state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.LOCK)) {
            setItemStack(
                0,
                getProperMaterial(material),
                isNotProtected
                    ? TranslationKey.INVENTORIES__LOCK
                    : TranslationKey.INVENTORIES__UNLOCK
            );
        }

        if (!isNotProtected && state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.MANAGER)) {
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
            setItemStack(
                3,
                Material.NAME_TAG,
                TranslationKey.INVENTORIES__SET_BLOCK_NAME
            );
            if (PlayerInventoryClipboard.contains(player.getUniqueId().toString())) {
                setItemStack(
                    getSize() - 4,
                    Material.KNOWLEDGE_BOOK,
                    TranslationKey.INVENTORIES__PASTE_CONFIGURATION
                );
            }
            setItemStack(
                getSize() - 3,
                Material.PAPER,
                TranslationKey.INVENTORIES__COPY_CONFIGURATION
            );
        }

        if (!isNotProtected && state.menuPermissions.contains(BlockAccessMenuEvent.MenuPermission.INFO)) {
            setItemStack(
                getSize() - 2,
                Material.OAK_SIGN,
                TranslationKey.INVENTORIES__BLOCK_INFO
            );
        }
        setBackButton();
        return inventory;
    }
}
