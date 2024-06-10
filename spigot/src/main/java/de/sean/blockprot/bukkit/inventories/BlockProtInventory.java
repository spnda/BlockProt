/*
 * Copyright (C) 2021 - 2023 spnda
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
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import de.sean.blockprot.bukkit.nbt.FriendSupportingHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.nbt.FriendModifyAction;
import de.sean.blockprot.nbt.LockReturnValue;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
    protected Inventory inventory;

    /**
     * Creates a new inventory using {@link BlockProtInventory#createInventory()}.
     * To avoid having the inventory created unconditionally, use {@link BlockProtInventory#BlockProtInventory(boolean)}.
     *
     * @since 0.2.2
     */
    @Deprecated
    public BlockProtInventory() {
        inventory = createInventory();
    }

    /**
     * Creates a new BlockProtInventory object, that optionally also has its inventory initialized directly.
     *
     * @param createInventory Whether the inventory should be instantly created.
     * @since 1.1.7
     */
    public BlockProtInventory(boolean createInventory) {
        if (createInventory)
            inventory = createInventory();
    }

    /**
     * Gets this holder's inventory. Does not create a new one.
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
    @Nullable
    abstract String getTranslatedInventoryName();

    /**
     * Get the default inventory name.
     *
     * @return The translated inventory name, or this class's simple name if the translation was not
     * found.
     * @since 0.2.2
     */
    @Nullable
    public final String getDefaultInventoryName() {
        final var inventoryName = getTranslatedInventoryName();
        if (inventoryName == null)
            return null;
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
     * returns an empty String, this class's simple name will be used. If it returns null, this uses
     * the default 'Chest' title.
     *
     * @return The Bukkit Inventory.
     * @since 0.2.2
     */
    @NotNull
    protected final Inventory createInventory() {
        var name = getDefaultInventoryName();
        if (name != null) {
            return Bukkit.createInventory(this, getSize(), name);
        } else {
            return Bukkit.createInventory(this, getSize());
        }
    }

    /**
     * Creates a new inventory with given {@code title}.
     *
     * @param title The title of the inventory. Should be already
     *              translated.
     * @return The new Bukkit inventory.
     * @since 1.0.4
     */
    @NotNull
    protected final Inventory createInventory(@NotNull String title) {
        return Bukkit.createInventory(this, getSize(), title);
    }

    /**
     * Modifies given {@code friend} for given {@code action}.
     *
     * @param player The player, or better the owner of the block we want to modify
     *               or the player we want to edit the default friends for.
     * @param friend The friend we want to do {@code action} for.
     * @param action The action to perform with {@code friend}.
     * @since 0.4.7
     */
    @Deprecated
    protected final void modifyFriendsForAction(
        @NotNull final Player player,
        @NotNull final OfflinePlayer friend,
        @NotNull final FriendModifyAction action
    ) {
        applyChanges(
            player,
            (handler) -> handler.modifyFriends(
                player.getUniqueId().toString(),
                friend.getUniqueId().toString(),
                action
            ),
            (handler) -> {
                switch (action) {
                    case ADD_FRIEND -> handler.addFriend(friend.getUniqueId().toString());
                    case REMOVE_FRIEND -> handler.removeFriend(friend.getUniqueId().toString());
                }
            }
        );
    }

    /**
     * Modifies given {@code friend} for given {@code action}.
     *
     * @param player The player, or better the owner of the block we want to modify
     *               or the player we want to edit the default friends for.
     * @param friend The friend's UUID we want to do {@code action} for.
     * @param action The action to perform with {@code friend}.
     * @since 1.1.16
     */
    protected final void modifyFriendsForAction(
            @NotNull final Player player,
            @NotNull final UUID friend,
            @NotNull final FriendModifyAction action
    ) {
        applyChanges(
            player,
            (handler) -> handler.modifyFriends(
                player.getUniqueId().toString(),
                friend.toString(),
                action
            ),
            (handler) -> {
                switch (action) {
                    case ADD_FRIEND -> handler.addFriend(friend.toString());
                    case REMOVE_FRIEND -> handler.removeFriend(friend.toString());
                }
            }
        );
    }

    /**
     * Creates a temporary {@link BlockNBTHandler} or {@link PlayerSettingsHandler}
     * depending on {@link InventoryState#friendSearchState},
     *
     * @param player            The player we use to obtain the {@link InventoryState} for.
     * @param onBlockChanges    A callback to easily modify a {@link BlockNBTHandler}. Can
     *                          be null, if this path is not intended.
     * @param onSettingsChanges A callback to easily modify a {@link PlayerSettingsHandler}.
     *                          Can be null, if this path is not intended.
     * @since 0.4.7
     */
    protected void applyChanges(
        @NotNull final Player player,
        @Nullable final Function<BlockNBTHandler, LockReturnValue> onBlockChanges,
        @Nullable final Consumer<PlayerSettingsHandler> onSettingsChanges) {
        InventoryState state = InventoryState.get(player.getUniqueId());
        switch (state.friendSearchState) {
            case FRIEND_SEARCH -> {
                if (onBlockChanges == null) return;
                assert state.getBlock() != null;
                BlockNBTHandler nbtHandler = getNbtHandlerOrNull(state.getBlock());
                if (nbtHandler == null) return;
                LockReturnValue ret = onBlockChanges.apply(nbtHandler);
                if (ret.success)
                    nbtHandler.applyToOtherContainer();
            }
            case DEFAULT_FRIEND_SEARCH -> {
                if (onSettingsChanges == null) return;
                PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
                onSettingsChanges.accept(settingsHandler);
            }
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
     * Sets an enchanted item stack in this inventory at {@code index}.
     *
     * @param index The index of the item in the inventory.
     * @param material The material of the item.
     * @param key The translation key for the display name of the item.
     * @param value Whether the enchantment should be added.
     * @since 0.4.13
     * @see #setEnchantedOptionItemStack(int, Material, TranslationKey, boolean)
     */
    public void setEnchantedItemStack(int index, Material material, TranslationKey key, boolean value) {
        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) Bukkit.getItemFactory().getItemMeta(material);
        if (meta != null) {
            meta.setDisplayName(Translator.get(key));
            stack.setItemMeta(meta);
        }
        toggleEnchants(stack, value);
        inventory.setItem(index, stack);
    }

    /**
     * Set a player skull to {@code index}.
     *
     * @param index  The index of the skull inside this inventory.
     * @param player The player whose skull should be used.
     * @since 0.3.2
     * @deprecated Use setPlayerSkull(int, PlayerProfile) instead.
     */
    @Deprecated
    public void setPlayerSkull(int index, OfflinePlayer player) {
        final ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta == null) {
            meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
        }

        try {
            assert meta != null;
            meta.setOwningPlayer(player);
            meta.setDisplayName(player.getName());
        } catch (Exception e) {
            BlockProt.getInstance().getLogger().severe("Failed to set skull head for \"" + player.getName() + "\": " + e.getMessage());
        }

        stack.setItemMeta(meta);
        inventory.setItem(index, stack);
    }

    /**
     * Set a player skull to {@code index}.
     *
     * @param index  The index of the skull inside this inventory.
     * @param profile The player profile whose skull should be used.
     * @since 1.1.16
     */
    public void setPlayerSkull(int index, @Nullable final PlayerProfile profile) {
        final var stack = new ItemStack(Material.PLAYER_HEAD, 1);
        var meta = (SkullMeta) stack.getItemMeta();
        if (meta == null)
            meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);

        try {
            assert meta != null;
            meta.setOwnerProfile(profile);
            if (profile != null && profile.getName() != null)
                meta.setDisplayName(profile.getName());
        } catch (Exception e) {
            BlockProt.getInstance().getLogger().severe("Failed to set skull head for \"" + (profile == null ? "" : profile.getName()) + "\": " + e.getMessage());
        }

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
    protected void closeAndOpen(@NotNull final HumanEntity player, @Nullable final Inventory inventory) {
        if (inventory != null) {
            player.openInventory(inventory);
        } else {
            player.closeInventory();
            InventoryState.remove(player.getUniqueId());
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
        return switch (material) {
            case ACACIA_WALL_SIGN -> Material.ACACIA_SIGN;
            case BAMBOO_WALL_SIGN -> Material.BAMBOO_SIGN;
            case BIRCH_WALL_SIGN -> Material.BIRCH_SIGN;
            case CHERRY_WALL_SIGN -> Material.CHERRY_SIGN;
            case CRIMSON_WALL_SIGN -> Material.CRIMSON_SIGN;
            case DARK_OAK_WALL_SIGN -> Material.DARK_OAK_SIGN;
            case JUNGLE_WALL_SIGN -> Material.JUNGLE_SIGN;
            case MANGROVE_WALL_SIGN -> Material.MANGROVE_SIGN;
            case OAK_WALL_SIGN -> Material.OAK_SIGN;
            case SPRUCE_WALL_SIGN -> Material.SPRUCE_SIGN;
            case WARPED_WALL_SIGN -> Material.WARPED_SIGN;

            case ACACIA_WALL_HANGING_SIGN -> Material.ACACIA_HANGING_SIGN;
            case BAMBOO_WALL_HANGING_SIGN -> Material.BAMBOO_HANGING_SIGN;
            case BIRCH_WALL_HANGING_SIGN -> Material.BIRCH_HANGING_SIGN;
            case CHERRY_WALL_HANGING_SIGN -> Material.CHERRY_HANGING_SIGN;
            case CRIMSON_WALL_HANGING_SIGN-> Material.CRIMSON_HANGING_SIGN;
            case DARK_OAK_WALL_HANGING_SIGN -> Material.DARK_OAK_HANGING_SIGN;
            case JUNGLE_WALL_HANGING_SIGN -> Material.JUNGLE_HANGING_SIGN;
            case MANGROVE_WALL_HANGING_SIGN -> Material.MANGROVE_HANGING_SIGN;
            case OAK_WALL_HANGING_SIGN -> Material.OAK_HANGING_SIGN;
            case SPRUCE_WALL_HANGING_SIGN -> Material.SPRUCE_HANGING_SIGN;
            case WARPED_WALL_HANGING_SIGN -> Material.WARPED_HANGING_SIGN;
            default -> material;
        };
    }

    /**
     * This gets the NBT Handler for the block requested by the player. If
     * a {@link RuntimeException} is triggered due to the block being invalid,
     * possibly by another player breaking it, we return null, indicating the
     * caller should close the inventory.
     *
     * @param block The block
     * @return The nbt handler, or null, if none.
     */
    @Nullable
    protected BlockNBTHandler getNbtHandlerOrNull(@Nullable Block block) {
        if (block == null) return null;

        try {
            return new BlockNBTHandler(block);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Gets the handler for the block opened/edited by the player.
     * 
     * @since 1.0.0
     */
    protected @Nullable FriendSupportingHandler<NBTCompound> getFriendSupportingHandler(@NotNull InventoryState.FriendSearchState state,
                                                                                        @Nullable Player player,
                                                                                        @Nullable Block block) {
        return switch (state) {
            case FRIEND_SEARCH -> block == null ? null : getNbtHandlerOrNull(block);
            case DEFAULT_FRIEND_SEARCH -> player == null ? null : new PlayerSettingsHandler(player);
        };
    }

    /**
     * Adds an enchantment to {@code stack} and then hides it from
     * the player.
     * @param stack The stack to "enchant".
     * @since 0.4.13
     */
    @Deprecated
    protected ItemStack toggleEnchants(@NotNull final ItemStack stack) {
        return toggleEnchants(stack, null);
    }

    /**
     * Adds an enchantment to {@code stack} and then hides it from
     * the player.
     * @param stack The stack to "enchant".
     * @param toggle The value to toggle to. Can be null, to just switch its
     *               current value.
     * @since 0.4.13
     */
    @NotNull
    @Deprecated
    protected ItemStack toggleEnchants(@NotNull ItemStack stack, final @Nullable Boolean toggle) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(stack.getType());
        }
        if (meta != null) {
            if (meta.hasEnchants() && (toggle == null || !toggle)) {
                meta.removeEnchant(Enchantment.ARROW_INFINITE);
            } else if (!meta.hasEnchants() && (toggle == null || toggle)) {
                meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Updates the title of this inventory. As inventory titles cannot
     * be updated directly, this instead re-creates and re-opens the
     * inventory.
     *
     * @param title The new title of the inventory. Should already
     *              be translated.
     * @since 1.0.4
     */
    protected void updateTitle(@NotNull Player player, @NotNull String title) {
        this.inventory = this.createInventory(title);
        this.closeAndOpen(player, this.inventory);
    }

    /**
     * Sets an enchanted version of the material into the index. Based on the value of {@code value},
     * the string returned for the translation key will be suffixed with an ": Enabled", or
     * ": Disabled", as per the respective translation key for each.
     *
     * @param index The index of the item in the inventory.
     * @param material The material of the item.
     * @param key The translation key for the display name of the item.
     * @param value Whether the option is enabled. Controls the suffix and if the item is enchanted.
     * @since 1.1.8
     */
    public void setEnchantedOptionItemStack(int index, Material material, TranslationKey key, boolean value) {
        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) Bukkit.getItemFactory().getItemMeta(material);
        if (meta != null) {
            meta.setDisplayName(Translator.get(key) + ": " +
                (value ? Translator.get(TranslationKey.ENABLED) : Translator.get(TranslationKey.DISABLED)));
            stack.setItemMeta(meta);
        }
        toggleOption(stack, value);
        inventory.setItem(index, stack);
    }

    /**
     * Toggles the option name, if it's found at the end of the display name, and the enchantment
     * status of the given itemstack.
     *
     * @param stack The stack to "enchant" and change the name of.
     * @param toggle The value to toggle to. Can be null, to just switch its
     *               current value.
     * @since 1.1.8
     */
    @NotNull
    protected ItemStack toggleOption(@NotNull ItemStack stack, final @Nullable Boolean toggle) {
        var meta = stack.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(stack.getType());
        }
        if (meta != null) {
            var name = meta.getDisplayName();
            final var pos = name.lastIndexOf(':');
            if (pos != -1) {
                name = name.substring(0, pos);
            }

            if (meta.hasEnchants() && (toggle == null || !toggle)) {
                meta.removeEnchant(Enchantment.ARROW_INFINITE);
                meta.setDisplayName(name + ": " + Translator.get(TranslationKey.DISABLED));
            } else if (!meta.hasEnchants() && (toggle == null || toggle)) {
                meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                meta.setDisplayName(name + ": " + Translator.get(TranslationKey.ENABLED));
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
