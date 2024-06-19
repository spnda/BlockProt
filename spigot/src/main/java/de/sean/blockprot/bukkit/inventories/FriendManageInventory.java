/*
 * Copyright (C) 2021 - 2024 spnda
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

import com.google.common.collect.Iterables;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.inventories.InventoryState.FriendSearchState;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendSupportingHandler;
import de.sean.blockprot.nbt.LockReturnValue;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class FriendManageInventory extends BlockProtInventory {
    private final int maxSkulls = getSize() - InventoryConstants.singleLine;

    @Override
    public int getSize() {
        return InventoryConstants.sextupletLine; // 6 Lines of inventory go brr
    }

    @NotNull
    @Override
    public String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__MANAGE);
    }

    /**
     * Exits this inventory depending on {@code state}'s {@link FriendSearchState} back to the {@link BlockLockInventory}
     * or the {@link UserSettingsInventory} respectively.
     *
     * @param player The player to open/close the inventory for.
     * @param state  The {@code player}'s state.
     */
    public void exitModifyInventory(@NotNull final Player player, @NotNull final InventoryState state) {
        Inventory newInventory;
        switch (state.friendSearchState) {
            case FRIEND_SEARCH -> {
                if (state.getBlock() == null) return;
                BlockNBTHandler handler = getNbtHandlerOrNull(state.getBlock());
                if (handler == null) {
                    newInventory = null;
                } else {
                    newInventory = new BlockLockInventory()
                            .fill(
                                player,
                                state.getBlock().getState().getType(),
                                handler);
                }
            }
            case DEFAULT_FRIEND_SEARCH -> newInventory = new UserSettingsInventory().fill(player);
            default -> {
                return;
            }
        }
        closeAndOpen(player, newInventory);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE -> {
                // Exit the friend modify inventory and return to the base lock inventory.
                state.currentPageIndex = 0;
                exitModifyInventory(player, state);
            }
            case CYAN_STAINED_GLASS_PANE -> {
                if (state.currentPageIndex >= 1) {
                    state.currentPageIndex--;

                    closeAndOpen(player, fill(player));
                }
            }
            case BLUE_STAINED_GLASS_PANE -> {
                ItemStack lastFriendInInventory = event.getInventory().getItem(maxSkulls - 1);
                if (lastFriendInInventory != null && lastFriendInInventory.getAmount() != 0) {
                    // There's an item in the last slot => The page is fully filled up, meaning
                    // we should go to the next page.
                    state.currentPageIndex++;

                    closeAndOpen(player, fill(player));
                }
            }
            case SKELETON_SKULL, PLAYER_HEAD -> {
                // Get the clicked player head and open the detail inventory.
                final var index = findItemIndex(item);
                if (index >= 0 && index < state.friendResultCache.size()) {
                    state.currentFriend = state.friendResultCache.get(index);
                    var inv = new FriendDetailInventory().fill(player);
                    closeAndOpen(player, inv);
                }
            }
            case MAP -> FriendSearchInventory.openAnvilInventory(player);
            case BOOK -> closeAndOpen(player, new FriendSearchHistoryInventory().fill(player));
            case WITHER_SKELETON_SKULL -> {
                applyChanges(
                    player,
                    (handler) -> {
                        handler.addEveryoneAsFriend();
                        return new LockReturnValue(true, null);
                    },
                    FriendSupportingHandler::addEveryoneAsFriend
                );
                fill(player); // Essentially rebuilds the inventory.
            }
            default -> closeAndOpen(player, null); // Unexpected, exit the inventory.
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    @Nullable
    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        final @Nullable FriendSupportingHandler<NBTCompound> handler =
            getFriendSupportingHandler(state.friendSearchState, player, state.getBlock());
        if (handler == null) return null;

        var friends = handler.getFriends();

        // Fill the first page inventory with skeleton skulls.
        // We call fill() with the page buttons on this same holder. Clear the inventory too.
        state.friendResultCache.clear();
        this.inventory.clear();

        var hasAddedPublic = false;
        for (final var friend : friends) {
            if (friend.doesRepresentPublic())
                hasAddedPublic = true;
        }

        var pageOffset = maxSkulls * state.currentPageIndex;
        for (int i = 0; i < Math.min(friends.size() - pageOffset, maxSkulls); i++) {
            final var uuid = friends.get(pageOffset + i).getName();

            if (friends.get(pageOffset + i).doesRepresentPublic()) {
                this.setItemStack(i, Material.PLAYER_HEAD,
                    TranslationKey.INVENTORIES__FRIENDS__THE_PUBLIC,
                    List.of(Translator.get(TranslationKey.INVENTORIES__FRIENDS__THE_PUBLIC_DESC)));
            } else {
                this.setItemStack(i, Material.SKELETON_SKULL, uuid);
            }
            state.friendResultCache.add(UUID.fromString(uuid));
        }

        setItemStack(
            maxSkulls,
            Material.CYAN_STAINED_GLASS_PANE,
            TranslationKey.INVENTORIES__LAST_PAGE);
        setItemStack(
            maxSkulls + 1,
            Material.BLUE_STAINED_GLASS_PANE,
            TranslationKey.INVENTORIES__NEXT_PAGE);

        if (!hasAddedPublic) {
            setItemStack(
                getSize() - 4,
                Material.WITHER_SKELETON_SKULL,
                TranslationKey.INVENTORIES__FRIENDS__MAKE_PUBLIC);
        }
        setItemStack(
            getSize() - 3,
            Material.BOOK,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH_HISTORY);
        setItemStack(
            getSize() - 2,
            Material.MAP,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH);
        setBackButton();

        Bukkit.getScheduler().runTaskAsynchronously(
            BlockProt.getInstance(),
            () -> {
                try {
                    final var profiles = BlockProt.getProfileService().findAllByUuid(state.friendResultCache);

                    int i = 0;
                    while (i < Math.min(maxSkulls, profiles.size())) {
                        final var profile = profiles.get(i);
                        // The profiles array doesn't necessarily have the same order as the friendResultCache.
                        final var index = Iterables.indexOf(state.friendResultCache, f -> f.equals(profile.getUniqueId()));

                        if (!profile.getUniqueId().equals(FriendSupportingHandler.publicUuid)) {
                            setPlayerSkull(index, Bukkit.getServer().createPlayerProfile(profile.getUniqueId(), profile.getName()));
                        }
                        i++;
                    }
                } catch (Exception e) {
                    BlockProt.getInstance().getLogger().warning("Failed to update PlayerProfile: " + e.getMessage());
                }
            });

        return inventory;
    }
}
