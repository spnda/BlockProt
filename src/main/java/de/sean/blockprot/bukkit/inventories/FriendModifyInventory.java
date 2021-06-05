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

import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
import de.sean.blockprot.bukkit.nbt.FriendModifyAction;
import de.sean.blockprot.bukkit.util.LockUtil;
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
                    LockUtil.INSTANCE.getDoubleChest(state.getBlock(), player.getWorld());
                applyChanges(
                    state.getBlock(),
                    player,
                    exit,
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
                            new BlockLockHandler(state.getBlock()));
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
