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

import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import de.sean.blockprot.bukkit.nbt.*;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A base inventory holder for each of the plugins inventories.
 *
 * @since 0.2.2
 */
public abstract class BlockProtInventory implements InventoryHolder {
    /**
     * @since 0.2.2
     */
    protected final Inventory inventory;

    /**
     * Creates a new inventory using {@link BlockProtInventory#createInventory()}.
     *
     * @since 0.2.2
     */
    public BlockProtInventory() {
        inventory = createInventory();
    }

    /**
     * Get's this holder's inventory. Does not create a new one.
     *
     * @return The inventory for this holder.
     * @since 0.2.2
     */
    @NotNull
    @Override
    public final Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the size of this inventory, which should be a multiple of 9 with a maximum value of 6 *
     * 9.
     *
     * @return The size of this inventory.
     * @since 0.2.2
     */
    abstract int getSize();

    /**
     * Gets the translated name of this inventory, or an empty String if not translatable, by
     * the default TranslationKey for this inventory holder.
     *
     * @return The translated inventory name, or an empty String.
     * @since 0.2.2
     */
    @NotNull
    abstract String getTranslatedInventoryName();

    /**
     * Get the default inventory name.
     *
     * @return The translated inventory name, or this class's simple name if the translation was not
     * found.
     * @since 0.2.2
     */
    @NotNull
    public final String getDefaultInventoryName() {
        final String inventoryName = getTranslatedInventoryName();
        return inventoryName.isEmpty() ? this.getClass().getSimpleName() : inventoryName;
    }

    /**
     * Handles clicks in this inventory.
     *
     * @param event Bukkit's inventory click event for this inventory.
     * @param state The current players inventory state.
     * @since 0.2.3
     */
    public abstract void onClick(@NotNull final InventoryClickEvent event, @NotNull final InventoryState state);

    /**
     * Callback when this inventory gets closed, so that the holders can save their NBT or state.
     *
     * @param event Bukkit's inventory close event for this inventory.
     * @param state The current players inventory state.
     * @since 0.2.3
     */
    public abstract void onClose(@NotNull final InventoryCloseEvent event, @NotNull final InventoryState state);

    /**
     * Create this current inventory. If {@link BlockProtInventory#getTranslatedInventoryName()}
     * returns an empty String, this class's simple name will be used.
     *
     * @return The Bukkit Inventory.
     * @since 0.2.2
     */
    @NotNull
    protected final Inventory createInventory() {
        return Bukkit.createInventory(this, getSize(), getDefaultInventoryName());
    }

    /**
     * Allows quick modification of the default friends list with the {@code modify} callback.
     *
     * @param player The player who we want to modify the default friends for.
     * @param modify The callback function in which the given list can be modified.
     * @since 0.3.2
     */
    protected void modifyDefaultFriends(
        @NotNull final Player player, @NotNull final Function<List<String>, ?> modify) {
        final PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
        List<String> currentFriends = settingsHandler.getDefaultFriends();
        modify.apply(currentFriends);
        settingsHandler.setDefaultFriends(currentFriends);
    }

    /**
     * Modifies given {@code friend} for given {@code action}.
     *
     * @param state  The current inventory state for {@code player}.
     * @param player The player, or better the owner of the block we want to modify
     *               or the player we want to edit the default friends for.
     * @param friend The friend we want to do {@code action} for.
     * @param action The action to perform with {@code friend}.
     * @since 0.3.2
     */
    protected final void modifyFriendsForAction(
        @NotNull final InventoryState state,
        @NotNull final Player player,
        @NotNull final OfflinePlayer friend,
        @NotNull final FriendModifyAction action
    ) {
        final InventoryState.FriendSearchState searchState = state.getFriendSearchState();
        if (searchState == InventoryState.FriendSearchState.FRIEND_SEARCH) {
            if (state.getBlock() == null) return;
            final BlockState doubleChest = LockUtil.getDoubleChest(state.getBlock(), player.getWorld());
            applyChanges(
                state.getBlock(),
                player,
                false,
                true,
                (handler) -> {
                    final NBTTileEntity doubleChestNBT = doubleChest == null ? null : new NBTTileEntity(doubleChest);
                    return handler.modifyFriends(
                        player.getUniqueId().toString(),
                        friend.getUniqueId().toString(),
                        action,
                        doubleChestNBT
                    );
                }
            );
        } else if (searchState == InventoryState.FriendSearchState.DEFAULT_FRIEND_SEARCH) {
            modifyDefaultFriends(
                player,
                (l) -> {
                    switch (action) {
                        case ADD_FRIEND:
                            return l.add(friend.getUniqueId().toString());
                        case REMOVE_FRIEND:
                            return l.remove(friend.getUniqueId().toString());
                        default:
                            return null;
                    }
                }
            );
        }
    }

    /**
     * Creates a temporary {@link BlockNBTHandler} and allows the callback
     * {@code changes} to apply any changes using the handler.
     *
     * @param block       The block to use for the handler.
     * @param player      The player to send the message to or exit the inventory of.
     * @param exit        Whether we should exit the inventory using {@link #closeAndOpen(Player, Inventory)}.
     * @param sendMessage Whether to send the message from the {@link LockReturnValue}
     *                    returned by the callback.
     * @param changes     The callback.
     * @since 0.2.2
     */
    protected void applyChanges(
        @NotNull final Block block,
        @NotNull final Player player,
        final boolean exit,
        final boolean sendMessage,
        @NotNull final Function<BlockNBTHandler, LockReturnValue> changes) {
        BlockNBTHandler handler = new BlockNBTHandler(block);
        LockReturnValue ret = changes.apply(handler);
        if (ret.success) {
            handler.applyToDoor(handler.block);
        }
        if (sendMessage) {
            BaseComponent[] messageComponent = TextComponent.fromLegacyText(ret.message);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, messageComponent);
        }
        if (exit) {
            closeAndOpen(player, null);
        }
    }

    /**
     * Finds the index inside of this inventory for given item.
     *
     * @param item The item to check for. Every item in this inventory will be checked against this
     *             using {@link ItemStack#equals(Object)}
     * @return The index of the item inside the inventory. If not found, {@code -1}.
     * @since 0.2.2
     */
    protected int findItemIndex(@NotNull final ItemStack item) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = contents[i];
            if (stack == null) continue;
            if (stack.equals(item)) return i;
        }
        return -1;
    }

    /**
     * Map a {@link Stream} of {@link String} to a {@link List} of {@link OfflinePlayer} using
     * {@link UUID#fromString(String)} and {@link Bukkit#getOfflinePlayer(UUID)}.
     *
     * @param friends A {@link Stream} of {@link String} in which each entry should be parseable
     *                by {@link UUID#fromString(String)}.
     * @return A list of all players that mapped successfully.
     * @since 0.3.0
     */
    @NotNull
    protected final List<OfflinePlayer> mapFriendsToPlayer(@NotNull final Stream<FriendHandler> friends) {
        return friends
            .map((f) -> Bukkit.getOfflinePlayer(UUID.fromString(f.getName())))
            .collect(Collectors.toList());
    }

    /**
     * Allows for quick filtering of a list of {@link String}s for a list of {@link OfflinePlayer}.
     *
     * @param <T>        The type of the {@code filterList} and is the type used for the
     *                   returned list.
     * @param <U>        The type of the {@code input} list, which we check for each
     *                   entry in {@code filterList}.
     * @param input      A list of {@code U} that can be accessed inside of the
     *                   callback for validation.
     * @param filterList A list of all players to filter by.
     * @param check      A callback function, allowing the caller to easily define custom filter logic.
     *                   If false, we shall filter the item out of the original list.
     * @return A list of all {@link OfflinePlayer} in {@code allPlayers} which were valid as by
     * {@code check}.
     * @since 0.3.2
     */
    @NotNull
    protected <T, U> List<T> filterList(
        @NotNull final List<U> input,
        @NotNull final List<T> filterList,
        @NotNull final BiFunction<T, List<U>, Boolean> check) {
        final List<T> ret = new ArrayList<>();
        for (T filterListEntry : filterList) {
            if (check.apply(filterListEntry, input)) ret.add(filterListEntry);
        }
        return ret;
    }

    /**
     * Sets the back button to the last item in the inventory.
     *
     * @since 0.2.3
     */
    public void setBackButton() {
        setBackButton(inventory.getSize() - 1);
    }

    /**
     * Sets the back button to the [index] in the inventory.
     *
     * @param index The index of the back button inside this inventory.
     * @since 0.2.3
     */
    public void setBackButton(int index) {
        setItemStack(index, Material.BLACK_STAINED_GLASS_PANE, TranslationKey.INVENTORIES__BACK);
    }

    /**
     * Sets a ItemStack with the type [material] and the name translated by [key] at [index].
     *
     * @param index    The index of the item inside this inventory.
     * @param material The material of the item.
     * @param key      The translation key to use to get the translated name for this item.
     * @since 0.2.3
     */
    public void setItemStack(int index, Material material, TranslationKey key) {
        setItemStack(index, material, Translator.get(key));
    }

    /**
     * Sets a ItemStack with the type {@code material} and the name translated by {@code text} with
     * {@code lore} at {@code index}.
     *
     * @param index    The index of the item inside this inventory.
     * @param material The material of the item.
     * @param key      The translatable text to use.
     * @param lore     The lore of item, where each entry in the list
     *                 is one line.
     * @since 0.2.3
     */
    public void setItemStack(int index, Material material, TranslationKey key, List<String> lore) {
        setItemStack(index, material, Translator.get(key), lore);
    }

    /**
     * Sets a ItemStack with the type {@code material} and the name as {@code text} at {@code index}.
     *
     * @param index    The index of the item inside this inventory.
     * @param material The material of the item.
     * @param text     The text or name of the item.
     * @since 0.4.0
     */
    public void setItemStack(int index, Material material, String text) {
        setItemStack(index, material, text, Collections.emptyList());
    }

    /**
     * Sets a ItemStack with the type {@code material} and the name as {@code text} with {@code lore}
     * at {@code index}.
     *
     * @param index    The index of the item inside this inventory.
     * @param material The material of the item.
     * @param text     The text or name of the item.
     * @param lore     The lore of item, where each entry in the list
     *                 is one line.
     * @since 0.2.3
     */
    public void setItemStack(int index, Material material, String text, List<String> lore) {
        final ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(material);
        }

        assert meta != null;
        meta.setDisplayName(text);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }

        stack.setItemMeta(meta);
        inventory.setItem(index, stack);
    }

    /**
     * Set a player skull to {@code index}.
     *
     * @param index  The index of the skull inside this inventory.
     * @param player The player whose skull should be used.
     * @since 0.3.2
     */
    public void setPlayerSkull(int index, OfflinePlayer player) {
        final ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta == null) {
            meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
        }

        assert meta != null;
        meta.setOwningPlayer(player);
        meta.setDisplayName(player.getName());

        stack.setItemMeta(meta);
        inventory.setItem(index, stack);
    }

    /**
     * Closes the player's current inventory and then opens the new
     * inventory. If inventory is null, we clear the inventory state.
     *
     * @param player    The player to close and open the inventories.
     * @param inventory The inventory we want to open. If null, we don't
     *                  open any new inventory.
     * @since 0.4.2
     */
    protected void closeAndOpen(@NotNull final Player player, @Nullable final Inventory inventory) {
        player.closeInventory();
        if (inventory != null) {
            player.openInventory(inventory);
        } else {
            InventoryState.Companion.remove(player.getUniqueId());
        }
    }

    /**
     * This gets the proper material for a block. For example, wall signs
     * have a different material, but that material does not have its own
     * item.
     *
     * @param material The material to fix.
     * @return The fixed material usable for the item in the inventory.
     * @since 0.4.2
     */
    protected Material getProperMaterial(Material material) {
        switch (material) {
            case ACACIA_WALL_SIGN:
                return Material.ACACIA_SIGN;
            case BIRCH_WALL_SIGN:
                return Material.BIRCH_SIGN;
            case CRIMSON_WALL_SIGN:
                return Material.CRIMSON_SIGN;
            case DARK_OAK_WALL_SIGN:
                return Material.DARK_OAK_SIGN;
            case JUNGLE_WALL_SIGN:
                return Material.JUNGLE_SIGN;
            case SPRUCE_WALL_SIGN:
                return Material.SPRUCE_SIGN;
            case OAK_WALL_SIGN:
                return Material.OAK_SIGN;
            case WARPED_WALL_SIGN:
                return Material.WARPED_SIGN;
            default:
                return material;
        }
    }
}
