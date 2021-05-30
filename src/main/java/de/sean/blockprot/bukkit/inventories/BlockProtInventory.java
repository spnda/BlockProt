package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
import de.sean.blockprot.bukkit.nbt.LockReturnValue;
import de.sean.blockprot.bukkit.nbt.LockUtil;
import de.sean.blockprot.util.ItemUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BlockProtInventory implements InventoryHolder {
    protected Inventory inventory;

    public BlockProtInventory() {
        inventory = createInventory();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the size of this inventory, which should be a multiple of 9 with a maximum value of 6 * 9.
     */
    abstract int getSize();

    /**
     * Get the translated name of this inventory, or an empty String if not translatable.
     */
    @NotNull
    abstract String getTranslatedInventoryName();

    /**
     * Get the default inventory name.
     * @return The translated inventory name, or this class's simple name if the translation was not found.
     */
    @NotNull
    public String getDefaultInventoryName() {
        final String inventoryName = getTranslatedInventoryName();
        return inventoryName.isEmpty() ? this.getClass().getSimpleName() : inventoryName;
    }

    /**
     * Handles clicks in this inventory.
     * @param event Bukkit's inventory click event for this inventory.
     * @param state The current players inventory state.
     */
    public abstract void onInventoryClick(@NotNull InventoryClickEvent event, @Nullable InventoryState state);

    /**
     * Callback when this inventory gets closed, so that the holders can save their NBT or state.
     * @param event Bukkit's inventory close event for this inventory.
     * @param state The current players inventory state.
     */
    public abstract void onClose(@NotNull InventoryCloseEvent event, @Nullable InventoryState state);

    /**
     * Create this current inventory. If {@link BlockProtInventory#getTranslatedInventoryName()} returns an empty String,
     * this class's simple name will be used.
     * @return The Bukkit Inventory.
     */
    @NotNull
    public Inventory createInventory() {
        return Bukkit.createInventory(this, getSize(), getDefaultInventoryName());
    }

    /**
     * Exits the currently open inventory for [player] and removes the InventoryState for [player].
     */
    public void exit(@NotNull Player player) {
        player.closeInventory();
        InventoryState.Companion.remove(player.getUniqueId());
    }

    /**
     * Allows quick modification of the NBT friends list with the {@code modify} callback.
     * @param exit If after modification we should close the inventory and clear the inventory state for {@code player}.
     * @param modify The callback function in which the given list can be modified.
     */
    void modifyFriends(@NotNull Player player, boolean exit, @NotNull Function<List<String>, ?> modify) {
        NBTCompound playerNBT = new NBTEntity(player).getPersistentDataContainer();
        List<String> currentFriends = LockUtil.parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE));
        modify.apply(currentFriends);
        playerNBT.setString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE, currentFriends.toString());
        if (exit) exit(player);
    }

    void applyChanges(@NotNull Block block, @NotNull Player player, boolean exit, @NotNull Function<BlockLockHandler, LockReturnValue> changes) {
        BlockLockHandler handler = new BlockLockHandler(block);
        LockReturnValue ret = changes.apply(handler);
        if (ret.getSuccess()) {
            LockUtil.INSTANCE.applyToDoor(handler, handler.getBlock());
            BaseComponent[] messageComponent = TextComponent.fromLegacyText(ret.getMessage());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, messageComponent);
        }
        if (exit) exit(player);
    }

    /**
     * Finds the index inside of this inventory for given item.
     * @param item The item to check for. Every item in this inventory will be checked against this using {@link ItemStack#equals(Object)}
     * @return The index of the item inside the inventory. If not found, {@code -1}.
     */
    int findItemIndex(@NotNull ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getContents()[i].equals(item)) return i;
        }
        return -1;
    }

    @NotNull
    List<OfflinePlayer> mapUuidToPlayer(@NotNull List<String> uuids) {
        return uuids
            .stream()
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
     * Sets a ItemStack with the type [material] and the name as [key] at [index].
     */
    public void setItemStack(int index, Material material, TranslationKey key) {
        setItemStack(index, material, Translator.get(key));
    }

    public void setItemStack(int index, Material material, String text) {
        inventory.setItem(index, ItemUtil.INSTANCE.getItemStack(1, material, text));
    }
}
