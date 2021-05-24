package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import de.sean.blockprot.bukkit.nbt.FriendModifyAction;
import de.sean.blockprot.util.InventoryExtensionsKt;
import de.sean.blockprot.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The detail inventory for managing a single friend and their permission.
 */
public final class FriendDetailInventory implements FriendModifyInventory {
    @NotNull
    public static final FriendDetailInventory INSTANCE = new FriendDetailInventory();

    @Override
    public int getSize() {
        return InventoryConstants.singleLine;
    }

    @NotNull
    @Override
    public String getInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__MANAGE);
    }

    @Override
    public void onInventoryClick(@NotNull InventoryClickEvent event, @Nullable InventoryState state) {
        final Player player = (Player)event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;

        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE: {
                if (state == null) return;
                player.openInventory(FriendsModifyInventory.INSTANCE.createInventoryAndFill(player));
                break;
            }
            case RED_STAINED_GLASS_PANE: {
                if (state == null) return;
                OfflinePlayer friend = state.getCurFriend();
                assert friend != null;
                modifyFriendsForAction(state, player, friend, FriendModifyAction.REMOVE_FRIEND, false);
                player.openInventory(FriendsModifyInventory.INSTANCE.createInventoryAndFill(player));
                break;
            }
            case PLAYER_HEAD: {
                break; // Don't do anything.
            }
            default: {
                player.closeInventory();
                InventoryState.Companion.remove(player.getUniqueId());
            }
        }
        event.setCancelled(true);
    }

    public Inventory createInventoryAndFill(@NotNull Player player) {
        final Inventory inv = createInventory();
        final InventoryState state = InventoryState.Companion.get(player.getUniqueId());
        if (state == null) return inv;

        inv.setItem(
            0,
            ItemUtil.INSTANCE.getPlayerSkull(Objects.requireNonNull(state.getCurFriend()))
        );
        InventoryExtensionsKt.setItemStack(
            inv,
            1,
            Material.RED_STAINED_GLASS_PANE,
            TranslationKey.INVENTORIES__FRIENDS__REMOVE
        );
        InventoryExtensionsKt.setBackButton(inv);

        return inv;
    }
}
