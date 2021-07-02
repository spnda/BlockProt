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

import de.sean.blockprot.BlockProt;
import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.inventories.InventoryState.FriendSearchState;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FriendManageInventory extends BlockProtInventory {
    private int maxSkulls = InventoryConstants.tripleLine - 4;

    @Override
    public int getSize() {
        return InventoryConstants.tripleLine;
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
    public final void exitModifyInventory(@NotNull final Player player, @NotNull final InventoryState state) {
        player.closeInventory();
        Inventory inventory;
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) return;
                inventory =
                    new BlockLockInventory()
                        .fill(
                            player,
                            state.getBlock().getState().getType(),
                            new BlockNBTHandler(state.getBlock()));
                break;
            }
            case DEFAULT_FRIEND_SEARCH:
                inventory = new UserSettingsInventory().fill(player);
                break;
            default:
                return;
        }
        player.openInventory(inventory);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE: {
                // Exit the modify inventory and return to the base lock inventory.
                exitModifyInventory(player, state);
                break;
            }
            case CYAN_STAINED_GLASS_PANE: {
                if (state.getFriendPage() >= 1) {
                    state.setFriendPage(state.getFriendPage() - 1);

                    player.closeInventory();
                    player.openInventory(fill(player));
                }
                break;
            }
            case BLUE_STAINED_GLASS_PANE: {
                ItemStack lastFriendInInventory = event.getInventory().getItem(maxSkulls);
                if (lastFriendInInventory != null && lastFriendInInventory.getAmount() == 0) {
                    // There's an item in the last slot => The page is fully filled up, meaning
                    // we should go to the next page.
                    state.setFriendPage(state.getFriendPage() + 1);

                    player.closeInventory();
                    player.openInventory(fill(player));
                }
                break;
            }
            case SKELETON_SKULL:
            case PLAYER_HEAD: {
                // Get the clicked player head and open the detail inventory.
                int index = findItemIndex(item);
                OfflinePlayer friend = state.getFriendResultCache().get(index);
                state.setCurFriend(friend);
                final Inventory inv = new FriendDetailInventory().fill(player);
                player.closeInventory();
                player.openInventory(inv);
                break;
            }
            case MAP: {
                FriendSearchInventory.INSTANCE.openAnvilInventory(player);
                break;
            }
            default: {
                // Unexpected, exit the inventory.
                player.closeInventory();
                InventoryState.Companion.remove(player.getUniqueId());
                break;
            }
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    @NotNull
    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.Companion.get(player.getUniqueId());
        if (state == null) return inventory;

        List<OfflinePlayer> players;
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                final BlockNBTHandler handler =
                    new BlockNBTHandler(Objects.requireNonNull(state.getBlock()));
                // Let the players be filtered by any plugin integration.
                players = PluginIntegration.filterFriends(
                    (ArrayList<OfflinePlayer>) mapFriendsToPlayer(handler.getFriendsStream()), player, state.getBlock());
                break;
            }
            case DEFAULT_FRIEND_SEARCH: {
                // We have 1 button less, as that button is only for blocks, which gives us room
                // for one more friend.
                maxSkulls += 1;
                final PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
                List<String> currentFriends = settingsHandler.getDefaultFriends();
                players = currentFriends
                    .stream()
                    .map((friend) -> Bukkit.getOfflinePlayer(UUID.fromString(friend)))
                    .collect(Collectors.toList());
                break;
            }
            default: {
                throw new RuntimeException(
                    "Could not build "
                        + this.getClass().getName()
                        + " due to invalid friend search state: "
                        + state.getFriendSearchState());
            }
        }

        // Fill the first page inventory with skeleton skulls.
        state.getFriendResultCache().clear();
        int pageOffset = maxSkulls * state.getFriendPage();
        for (int i = pageOffset; i < Math.min(players.size() - pageOffset, maxSkulls); i++) {
            final OfflinePlayer curPlayer = players.get(i);
            ((BlockProtInventory) Objects.requireNonNull(inventory.getHolder()))
                .setItemStack(1, Material.SKELETON_SKULL, curPlayer.getName());
            state.getFriendResultCache().add(curPlayer);
        }

        // Only show the page buttons if there's more than 1 page.
        if (state.getFriendPage() == 0 && players.size() >= maxSkulls) {
            setItemStack(
                maxSkulls,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE);
            setItemStack(
                maxSkulls + 1,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE);
        }

        setItemStack(
            InventoryConstants.tripleLine - 2,
            Material.MAP,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH);
        setBackButton();

        Bukkit.getScheduler().runTaskAsynchronously(
            BlockProt.getInstance(),
            () -> {
                int i = 0;
                while (i < maxSkulls && i < state.getFriendResultCache().size()) {
                    ((BlockProtInventory) Objects.requireNonNull(inventory.getHolder()))
                        .setPlayerSkull(i, state.getFriendResultCache().get(i));
                    i++;
                }
            });

        return inventory;
    }
}

