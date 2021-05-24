package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
import de.sean.blockprot.bukkit.nbt.FriendModifyAction;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface FriendModifyInventory extends BlockProtInventory {
    default void modifyFriendsForAction(@NotNull InventoryState state, @NotNull Player player, @NotNull OfflinePlayer friend, @NotNull FriendModifyAction action, boolean exit) {
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) break;
            }
            case DEFAULT_FRIEND_SEARCH: {
                modifyFriends(player, exit, (l) -> {
                    switch (action) {
                        case ADD_FRIEND:
                            return l.add(friend.getUniqueId().toString());
                        case REMOVE_FRIEND:
                            return l.remove(friend.getUniqueId().toString());
                        default:
                            return null;
                    }
                });
            }
        }
    }

    default void exitModifyInventory(@NotNull Player player, @NotNull InventoryState state) {
        player.closeInventory();
        Inventory inventory;
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) return;
                inventory = BlockLockInventory.INSTANCE.createInventoryAndFill(
                    player,
                    state.getBlock().getState().getType(),
                    new BlockLockHandler(state.getBlock())
                );
                break;
            }
            case DEFAULT_FRIEND_SEARCH: {
                inventory = UserSettingsInventory.INSTANCE.createInventoryAndFill(player);
                break;
            }
            default: {
                return;
            }
        }
        player.openInventory(inventory);
    }

    default List<OfflinePlayer> filterFriendsForOfflinePlayers(List<String> current, List<OfflinePlayer> allPlayers, String self) {
        final List<OfflinePlayer> ret = new ArrayList<>();
        for (OfflinePlayer player : allPlayers) {
            final String uuid = player.getUniqueId().toString();
            if (!current.contains(uuid) && !uuid.equals(self)) ret.add(player);
        }
        return ret;
    }
}
