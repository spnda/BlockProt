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
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import de.sean.blockprot.bukkit.nbt.LockReturnValue;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import de.sean.blockprot.bukkit.util.ItemUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BlockProtInventory implements InventoryHolder {
    protected final Inventory inventory;

    /**
     * Creates a new inventory using {@link BlockProtInventory#createInventory()}.
     */
    public BlockProtInventory() {
        inventory = createInventory();
    }

    /**
     * Get's this holder's inventory. Does not create a new one.
     *
     * @return The inventory for this holder.
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
     */
    abstract int getSize();

    /**
     * Gets the translated name of this inventory, or an empty String if not translatable, by
     * the default TranslationKey for this inventory holder.
     *
     * @return The translated inventory name, or an empty String.
     */
    @NotNull
    abstract String getTranslatedInventoryName();

    /**
     * Get the default inventory name.
     *
     * @return The translated inventory name, or this class's simple name if the translation was not
     * found.
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
     */
    public abstract void onClick(@NotNull final InventoryClickEvent event, @NotNull final InventoryState state);

    /**
     * Callback when this inventory gets closed, so that the holders can save their NBT or state.
     *
     * @param event Bukkit's inventory close event for this inventory.
     * @param state The current players inventory state.
     */
    public abstract void onClose(@NotNull final InventoryCloseEvent event, @NotNull final InventoryState state);

    /**
     * Create this current inventory. If {@link BlockProtInventory#getTranslatedInventoryName()}
     * returns an empty String, this class's simple name will be used.
     *
     * @return The Bukkit Inventory.
     */
    @NotNull
    protected final Inventory createInventory() {
        return Bukkit.createInventory(this, getSize(), getDefaultInventoryName());
    }

    /**
     * Exits the currently open inventory for {@code player} and removes the InventoryState
     * for {@code player}.
     *
     * @param player The player currently viewing this inventory.
     */
    public void exit(@NotNull final Player player) {
        player.closeInventory();
        InventoryState.Companion.remove(player.getUniqueId());
    }

    /**
     * Allows quick modification of the NBT friends list with the {@code modify} callback.
     *
     * @param exit   If after modification we should close the inventory and clear the inventory state
     *               for {@code player}.
     * @param modify The callback function in which the given list can be modified.
     */
    protected void modifyFriends(
        @NotNull final Player player, final boolean exit, @NotNull final Function<List<String>, ?> modify) {
        final PlayerSettingsHandler settingsHandler = new PlayerSettingsHandler(player);
        List<String> currentFriends = settingsHandler.getDefaultFriends();
        modify.apply(currentFriends);
        settingsHandler.setDefaultFriends(currentFriends);

        if (exit) {
            exit(player);
        }
    }

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
            exit(player);
        }
    }

    /**
     * Finds the index inside of this inventory for given item.
     *
     * @param item The item to check for. Every item in this inventory will be checked against this
     *             using {@link ItemStack#equals(Object)}
     * @return The index of the item inside the inventory. If not found, {@code -1}.
     */
    protected int findItemIndex(@NotNull final ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getContents()[i].equals(item)) return i;
        }
        return -1;
    }

    @NotNull
    protected final List<OfflinePlayer> mapFriendsToPlayer(@NotNull final List<FriendHandler> friends) {
        return mapFriendsToPlayer(friends.stream());
    }

    /**
     * Map a {@link Stream} of {@link String} to a {@link List} of {@link OfflinePlayer} using
     * {@link UUID#fromString(String)} and {@link Bukkit#getOfflinePlayer(UUID)}.
     * @param friends A {@link Stream} of {@link String} in which each entry should be parseable
     *        by {@link UUID#fromString(String)}.
     * @return A list of all players that mapped successfully.
     */
    @NotNull
    protected final List<OfflinePlayer> mapFriendsToPlayer(@NotNull final Stream<FriendHandler> friends) {
        return friends
            .map((f) -> Bukkit.getOfflinePlayer(UUID.fromString(f.getName())))
            .collect(Collectors.toList());
    }

    /**
     * Sets the back button to the last item in the inventory.
     */
    public void setBackButton() {
        setBackButton(inventory.getSize() - 1);
    }

    /**
     * Sets the back button to the [index] in the inventory.
     */
    public void setBackButton(int index) {
        setItemStack(index, Material.BLACK_STAINED_GLASS_PANE, TranslationKey.INVENTORIES__BACK);
    }

    /**
     * Sets a ItemStack with the type [material] and the name translated by [key] at [index].
     *
     * @param key The translation key to use to get the translated name for this item.
     */
    public void setItemStack(int index, Material material, TranslationKey key) {
        setItemStack(index, material, Translator.get(key));
    }

    /**
     * Sets a ItemStack with the type {@code material} and the name as {@code text} at {@code index}.
     */
    public void setItemStack(int index, Material material, String text) {
        inventory.setItem(index, ItemUtil.INSTANCE.getItemStack(1, material, text));
    }

    /**
     * Sets a ItemStack with the type {@code material} and the name translated by {@code text} with
     * {@code lore} at {@code index}.
     */
    public void setItemStack(int index, Material material, TranslationKey key, List<String> lore) {
        setItemStack(index, material, Translator.get(key), lore);
    }

    /**
     * Sets a ItemStack with the type {@code material} and the name as {@code text} with {@code lore}
     * at {@code index}.
     */
    public void setItemStack(int index, Material material, String text, List<String> lore) {
        inventory.setItem(index, ItemUtil.INSTANCE.getItemStack(1, material, text, lore));
    }
}
