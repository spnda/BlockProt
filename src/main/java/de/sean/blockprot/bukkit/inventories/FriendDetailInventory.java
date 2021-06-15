/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import de.sean.blockprot.bukkit.nbt.*;
import de.sean.blockprot.bukkit.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The detail inventory for managing a single friend and their permission.
 */
public final class FriendDetailInventory extends FriendModifyInventory {
    @NotNull
    private static final List<EnumSet<BlockAccessFlag>> accessFlagCombinations =
        Arrays.asList(
            EnumSet.of(BlockAccessFlag.READ),
            EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE));

    @NotNull
    private EnumSet<BlockAccessFlag> curFlags = EnumSet.noneOf(BlockAccessFlag.class);

    @Nullable
    private PlayerNBTHandler playerHandler = null;

    @Override
    public int getSize() {
        return InventoryConstants.singleLine;
    }

    @NotNull
    @Override
    public String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__EDIT);
    }

    private int getAccessFlagIndexOf(EnumSet<BlockAccessFlag> flags) {
        int result = 0;
        for (; result < accessFlagCombinations.size(); result++) {
            if (flags.equals(accessFlagCombinations.get(result))) return result;
        }
        return result;
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;

        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE: {
                player.openInventory(new FriendManageInventory().fill(player));
                break;
            }
            case RED_STAINED_GLASS_PANE: {
                OfflinePlayer friend = state.getCurFriend();
                assert friend != null;
                modifyFriendsForAction(
                    state, player, friend, FriendModifyAction.REMOVE_FRIEND, false);
                player.openInventory(new FriendManageInventory().fill(player));
                break;
            }
            case OAK_DOOR: {
                if (playerHandler == null) break;
                int curIndex = getAccessFlagIndexOf(playerHandler.getAccessFlags());

                if (curIndex + 1 >= accessFlagCombinations.size()) curIndex = 0;
                else curIndex += 1;
                curFlags = accessFlagCombinations.get(curIndex);
                setItemStack(
                    2,
                    Material.OAK_DOOR,
                    BlockAccessFlag.accessFlagToString(curFlags),
                    BlockAccessFlag.accumulateAccessFlagLore(curFlags)
                );
                break;
            }
            case PLAYER_HEAD: {
                break; // Don't do anything.
            }
            default:
                exit(player);
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        if (this.playerHandler != null)
            this.playerHandler.setAccessFlags(curFlags);
    }

    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.Companion.get(player.getUniqueId());
        if (state == null) return inventory;

        /* Get the current FriendPlayer */
        BlockNBTHandler handler = new BlockNBTHandler(Objects.requireNonNull(state.getBlock()));
        final Optional<FriendPlayer> friend =
            handler.getFriend(player.getUniqueId().toString());

        if (!friend.isPresent()) {
            Bukkit.getLogger().warning("Tried to open a " + this.getClass().getSimpleName() + " with a unknown player.");
            return inventory;
        }
        playerHandler = new PlayerNBTHandler(friend.get());

        inventory.setItem(
            0, ItemUtil.INSTANCE.getPlayerSkull(Objects.requireNonNull(state.getCurFriend())));
        setItemStack(
            1, Material.RED_STAINED_GLASS_PANE, TranslationKey.INVENTORIES__FRIENDS__REMOVE);

        if (state.getFriendSearchState() == InventoryState.FriendSearchState.FRIEND_SEARCH) {
            setItemStack(
                2,
                Material.OAK_DOOR,
                BlockAccessFlag.accessFlagToString(curFlags),
                BlockAccessFlag.accumulateAccessFlagLore(curFlags)
            );
        }

        setBackButton();

        return inventory;
    }
}
