package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
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

public abstract class FriendModifyInventory extends BlockProtInventory {
    public FriendModifyInventory() {
        super();
    }

    public void modifyFriendsForAction(@NotNull InventoryState state, @NotNull Player player, @NotNull OfflinePlayer friend, @NotNull FriendModifyAction action, boolean exit) {
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) break;
                final BlockState doubleChest = LockUtil.INSTANCE.getDoubleChest(state.getBlock(), player.getWorld());
                applyChanges(state.getBlock(), player, exit, (handler) -> handler.modifyFriends(
                    player.getUniqueId().toString(),
                    friend.getUniqueId().toString(),
                    action,
                    doubleChest == null ? null : new NBTTileEntity(doubleChest)
                ));
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

    public void exitModifyInventory(@NotNull Player player, @NotNull InventoryState state) {
        player.closeInventory();
        Inventory inventory;
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                if (state.getBlock() == null) return;
                inventory = new BlockLockInventory().fill(
                    player,
                    state.getBlock().getState().getType(),
                    new BlockLockHandler(state.getBlock())
                );
                break;
            }
            case DEFAULT_FRIEND_SEARCH: {
                inventory = new UserSettingsInventory().fill(player);
                break;
            }
            default: {
                return;
            }
        }
        player.openInventory(inventory);
    }

    List<OfflinePlayer> filterFriendsForOfflinePlayers(List<String> current, List<OfflinePlayer> allPlayers, String self) {
        final List<OfflinePlayer> ret = new ArrayList<>();
        for (OfflinePlayer player : allPlayers) {
            final String uuid = player.getUniqueId().toString();
            if (!current.contains(uuid) && !uuid.equals(self)) ret.add(player);
        }
        return ret;
    }
}
