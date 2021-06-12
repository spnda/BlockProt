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
import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
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
        @NotNull final Function<BlockLockHandler, LockReturnValue> changes) {
        BlockLockHandler handler = new BlockLockHandler(block);
        LockReturnValue ret = changes.apply(handler);
        if (ret.success) {
            handler.applyToDoor(handler.block);
            BaseComponent[] messageComponent = TextComponent.fromLegacyText(ret.message);
            if (sendMessage) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, messageComponent);
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
    protected final List<OfflinePlayer> mapUuidToPlayer(@NotNull final List<String> uuids) {
        return uuids.stream()
            .map((s) -> Bukkit.getOfflinePlayer(UUID.fromString(s)))
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
