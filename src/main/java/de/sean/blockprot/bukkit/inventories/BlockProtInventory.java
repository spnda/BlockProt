package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
import de.sean.blockprot.bukkit.nbt.LockReturnValue;
import de.sean.blockprot.bukkit.nbt.LockUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface BlockProtInventory {
    /**
     * Get the size of this inventory, which should be a multiple of 9 with a maximum value of 6 * 9.
     */
    int getSize();

    /**
     * Get the translated name of this inventory, or an empty String if not translatable.
     */
    @NotNull
    String getTranslatedInventoryName();

    /**
     * Get the default inventory name.
     * @return The translated inventory name, or this class's simple name if the translation was not found.
     */
    @NotNull
    default String getDefaultInventoryName() {
        final String inventoryName = getTranslatedInventoryName();
        return inventoryName.isEmpty() ? this.getClass().getSimpleName() : inventoryName;
    }

    /**
     * Handles clicks in this inventory.
     * @param event Bukkit's inventory click event for this inventory.
     * @param state The current players inventory state.
     */
    void onInventoryClick(@NotNull InventoryClickEvent event, @Nullable InventoryState state);

    /**
     * Create this current inventory. If {@link BlockProtInventory#getTranslatedInventoryName()} returns an empty String,
     * this class's simple name will be used.
     * @return The Bukkit Inventory.
     */
    @NotNull
    default Inventory createInventory() {
        return Bukkit.createInventory(null, getSize(), getDefaultInventoryName());
    }

    /**
     * Allows quick modification of the NBT friends list with the {@code modify} callback.
     * @param exit If after modification we should close the inventory and clear the inventory state for {@code player}.
     * @param modify The callback function in which the given list can be modified.
     */
    default void modifyFriends(@NotNull Player player, boolean exit, @NotNull Function<List<String>, ?> modify) {
        NBTCompound playerNBT = new NBTEntity(player).getPersistentDataContainer();
        List<String> currentFriends = LockUtil.parseStringList(playerNBT.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE));
        modify.apply(currentFriends);
        playerNBT.setString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE, currentFriends.toString());
        if (exit) {
            player.closeInventory();
            InventoryState.Companion.remove(player.getUniqueId());
        }
    }

    default void applyChanges(@NotNull Block block, @NotNull Player player, boolean exit, @NotNull Function<BlockLockHandler, LockReturnValue> changes) {
        BlockLockHandler handler = new BlockLockHandler(block);
        LockReturnValue ret = changes.apply(handler);
        if (ret.getSuccess()) {
            LockUtil.INSTANCE.applyToDoor(handler, handler.getBlock());
            BaseComponent[] messageComponent = TextComponent.fromLegacyText(ret.getMessage());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, messageComponent);
        }
        if (exit) {
            player.closeInventory();
            InventoryState.Companion.remove(player.getUniqueId());
        }
    }

    default int findItemIndex(@NotNull Inventory inventory, @NotNull ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getContents()[i].equals(item)) return i;
        }
        return -1;
    }

    @NotNull
    default List<OfflinePlayer> mapUuidToPlayer(@NotNull List<String> uuids) {
        return uuids
            .stream()
            .map((s) -> Bukkit.getOfflinePlayer(UUID.fromString(s)))
            .collect(Collectors.toList());
    }
}
