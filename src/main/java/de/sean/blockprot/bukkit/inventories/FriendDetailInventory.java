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
import de.sean.blockprot.bukkit.nbt.BlockAccessFlag;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import de.sean.blockprot.bukkit.nbt.FriendModifyAction;
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
public final class FriendDetailInventory extends BlockProtInventory {
    @NotNull
    private static final List<EnumSet<BlockAccessFlag>> accessFlagCombinations =
        Arrays.asList(
            EnumSet.of(BlockAccessFlag.READ),
            EnumSet.of(BlockAccessFlag.READ, BlockAccessFlag.WRITE));

    @Nullable
    private EnumSet<BlockAccessFlag> curFlags = EnumSet.noneOf(BlockAccessFlag.class);

    @Nullable
    private FriendHandler playerHandler = null;

    @Override
    public int getSize() {
        return InventoryConstants.singleLine;
    }

    @NotNull
    @Override
    public String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__EDIT);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;

        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE: {
                closeAndOpen(player, new FriendManageInventory().fill(player));
                break;
            }
            case RED_STAINED_GLASS_PANE: {
                OfflinePlayer friend = state.getCurFriend();
                assert friend != null;
                modifyFriendsForAction(
                    state, player, friend, FriendModifyAction.REMOVE_FRIEND);
                // We remove the friend, so the player does not exist anymore either.
                this.playerHandler = null;
                closeAndOpen(player, new FriendManageInventory().fill(player));
                break;
            }
            case ENDER_EYE: {
                if (playerHandler == null || curFlags == null) break;
                int curIndex = 0;
                for (; curIndex < accessFlagCombinations.size(); curIndex++) {
                    if (curFlags.equals(accessFlagCombinations.get(curIndex))) break;
                }

                if (curIndex + 1 >= accessFlagCombinations.size()) curIndex = 0;
                else curIndex += 1;
                curFlags = accessFlagCombinations.get(curIndex);
                setItemStack(
                    2,
                    Material.ENDER_EYE,
                    BlockAccessFlag.accessFlagToString(curFlags),
                    BlockAccessFlag.accumulateAccessFlagLore(curFlags)
                );
                break;
            }
            case PLAYER_HEAD: {
                break; // Don't do anything.
            }
            default:
                closeAndOpen(player, null);
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {
        if (this.playerHandler != null && curFlags != null)
            this.playerHandler.setAccessFlags(curFlags);
    }

    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.Companion.get(player.getUniqueId());
        if (state == null) return inventory;

        final OfflinePlayer friend = state.getCurFriend();
        if (friend == null) return inventory;

        setPlayerSkull(0, state.getCurFriend());
        setItemStack(
            1, Material.RED_STAINED_GLASS_PANE, TranslationKey.INVENTORIES__FRIENDS__REMOVE);

        if (state.getFriendSearchState() == InventoryState.FriendSearchState.FRIEND_SEARCH) {
            /* Get the current FriendHandler */
            BlockNBTHandler handler = new BlockNBTHandler(Objects.requireNonNull(state.getBlock()));
            final Optional<FriendHandler> friendHandler =
                handler.getFriend(friend.getUniqueId().toString());

            if (!friendHandler.isPresent()) {
                Bukkit.getLogger().warning("Tried to open a " + this.getClass().getSimpleName() + " with a unknown player.");
                return inventory;
            }
            playerHandler = friendHandler.get();

            /* Read the current access flags */
            curFlags = playerHandler.getAccessFlags();

            setItemStack(
                2,
                Material.ENDER_EYE,
                BlockAccessFlag.accessFlagToString(curFlags),
                BlockAccessFlag.accumulateAccessFlagLore(curFlags)
            );
        }

        setBackButton();

        return inventory;
    }
}
