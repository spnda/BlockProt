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

import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendModifyAction;
import de.sean.blockprot.bukkit.nbt.LockUtil;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class FriendModifyInventory extends BlockProtInventory {
    public FriendModifyInventory() {
        super();
    }

    /**
     * Modifies given {@code friend} for given {@code action}.
     *
     * @param state  The current inventory state for {@code player}.
     * @param player The player, or better the owner of the block we want to modify
     *               or the player we want to edit the default friends for.
     * @param friend The friend we want to do {@code action} for.
     * @param action The action to perform with {@code friend}.
     * @param exit   Whether we want to close the inventory for {@code player} after modifying.
     */
    public final void modifyFriendsForAction(
        @NotNull final InventoryState state,
        @NotNull final Player player,
        @NotNull final OfflinePlayer friend,
        @NotNull final FriendModifyAction action,
        final boolean exit) {
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) break;
                final BlockState doubleChest =
                    LockUtil.getDoubleChest(state.getBlock(), player.getWorld());
                applyChanges(
                    state.getBlock(),
                    player,
                    exit,
                    true,
                    (handler) ->
                        handler.modifyFriends(
                            player.getUniqueId().toString(),
                            friend.getUniqueId().toString(),
                            action,
                            doubleChest == null
                                ? null
                                : new NBTTileEntity(doubleChest)));
                break;
            }
            case DEFAULT_FRIEND_SEARCH: {
                modifyFriends(
                    player,
                    exit,
                    (l) -> {
                        switch (action) {
                            case ADD_FRIEND:
                                return l.add(friend.getUniqueId().toString());
                            case REMOVE_FRIEND:
                                return l.remove(friend.getUniqueId().toString());
                            default:
                                return null;
                        }
                    });
                break;
            }
            default:
                break;
        }
    }

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

    /**
     * Allows for quick filtering of a list of {@link String}s for a list of {@link OfflinePlayer}.
     *
     * @param input      A list of Strings, either name or UUIDs, that can be accessed inside of the
     *                   callback for validation.
     * @param allPlayers A list of all players to filter by.
     * @param check      A callback function, allowing the caller to easily define custom filter logic.
     * @return A list of all {@link OfflinePlayer} in {@code allPlayers} which were valid as by
     * {@code check}.
     */
    @NotNull
    List<OfflinePlayer> filterList(
        @NotNull final List<String> input,
        @NotNull final List<OfflinePlayer> allPlayers,
        @NotNull final BiFunction<String, List<String>, Boolean> check) {
        final List<OfflinePlayer> ret = new ArrayList<>();
        for (OfflinePlayer player : allPlayers) {
            final String playerUuid = player.getUniqueId().toString();
            if (check.apply(playerUuid, input)) ret.add(player);
        }
        return ret;
    }
}
