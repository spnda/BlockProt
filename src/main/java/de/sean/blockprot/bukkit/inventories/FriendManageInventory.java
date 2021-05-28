package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.BlockProt;
import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import de.sean.blockprot.bukkit.nbt.BlockLockHandler;
import de.sean.blockprot.bukkit.nbt.LockUtil;
import de.sean.blockprot.util.ItemUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class FriendManageInventory extends FriendModifyInventory {
    private final int maxSkulls = InventoryConstants.tripleLine - 4;

    @Override
    public int getSize() {
        return InventoryConstants.tripleLine;
    }

    @NotNull
    @Override
    public String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__MANAGE);
    }

    @Override
    public void onInventoryClick(@NotNull InventoryClickEvent event, @Nullable InventoryState state) {
        final Player player = (Player)event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE: {
                // Exit the modify inventory and return to the base lock inventory.
                if (state == null) break;
                exitModifyInventory(player, state);
                break;
            }
            case CYAN_STAINED_GLASS_PANE: {
                if (state != null && state.getFriendPage() >= 1) {
                    state.setFriendPage(state.getFriendPage() - 1);

                    player.closeInventory();
                    player.openInventory(fill(player));
                }
                break;
            }
            case BLUE_STAINED_GLASS_PANE: {
                ItemStack lastFriendInInventory = event.getInventory().getItem(maxSkulls);
                if (lastFriendInInventory != null && lastFriendInInventory.getAmount() == 0 && state != null) {
                    // There's an item in the last slot => The page is fully filled up, meaning we should go to the next page.
                    state.setFriendPage(state.getFriendPage() + 1);

                    player.closeInventory();
                    player.openInventory(fill(player));
                }
                break;
            }
            case SKELETON_SKULL:
            case PLAYER_HEAD: {
                // Get the clicked player head and open the detail inventory.
                if (state == null) break;
                int index = findItemIndex(item);
                OfflinePlayer friend = state.getFriendResultCache().get(index);
                state.setCurFriend(friend);
                final Inventory inv = new FriendDetailInventory().fill(player);
                player.closeInventory();
                player.openInventory(inv);
                break;
            }
            case MAP: {
                FriendSearchInventory.INSTANCE.openAnvilInventory(player);
                break;
            }
            default: {
                // Unexpected, exit the inventory.
                player.closeInventory();
                InventoryState.Companion.remove(player.getUniqueId());
                break;
            }
        }
        event.setCancelled(true);
    }

    @NotNull
    public Inventory fill(@NotNull Player player) {
        final InventoryState state = InventoryState.Companion.get(player.getUniqueId());
        if (state == null) return inventory;

        List<OfflinePlayer> players;
        switch (state.getFriendSearchState()) {
            case FRIEND_SEARCH: {
                final BlockLockHandler handler = new BlockLockHandler(Objects.requireNonNull(state.getBlock()));
                players = mapUuidToPlayer(handler.getAccess());
                break;
            }
            case DEFAULT_FRIEND_SEARCH: {
                final NBTCompound nbtEntity = new NBTEntity(player).getPersistentDataContainer();
                List<String> currentFriends = LockUtil.parseStringList(nbtEntity.getString(LockUtil.DEFAULT_FRIENDS_ATTRIBUTE));
                final String selfUuid = player.getUniqueId().toString();
                players = filterList(
                    currentFriends,
                    Arrays.asList(Bukkit.getOfflinePlayers()),
                    (uuid, cur) -> cur.contains(uuid) && !uuid.equals(selfUuid)
                );
                break;
            }
            default: {
                throw new RuntimeException("Could not build " + this.getClass().getName() + " due to invalid friend search state: " + state.getFriendSearchState());
            }
        }

        // Fill the first page inventory with skeleton skulls.
        state.getFriendResultCache().clear();
        int pageOffset = maxSkulls * state.getFriendPage();
        for (int i = pageOffset; i < Math.min(players.size() - pageOffset, maxSkulls); i++) {
            final OfflinePlayer curPlayer = players.get(i);
            inventory.setItem(i - pageOffset, ItemUtil.INSTANCE.getItemStack(1, Material.SKELETON_SKULL, curPlayer.getName()));
            state.getFriendResultCache().add(curPlayer);
        }

        // Only show the page buttons if there's more than 1 page.
        if (state.getFriendPage() == 0 && players.size() >= maxSkulls) {
            setItemStack(
                maxSkulls,
                Material.CYAN_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__LAST_PAGE
            );
            setItemStack(
                InventoryConstants.tripleLine - 3,
                Material.BLUE_STAINED_GLASS_PANE,
                TranslationKey.INVENTORIES__NEXT_PAGE
            );
        }
        setItemStack(
            InventoryConstants.tripleLine - 2,
            Material.MAP,
            TranslationKey.INVENTORIES__FRIENDS__SEARCH
        );
        setBackButton();

        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance, () -> {
            int i = 0;
            while (i < maxSkulls && i < state.getFriendResultCache().size()) {
                inventory.setItem(
                    i,
                    ItemUtil.INSTANCE.getPlayerSkull(state.getFriendResultCache().get(i))
                );
                i++;
            }
        });

        return inventory;
    }
}
