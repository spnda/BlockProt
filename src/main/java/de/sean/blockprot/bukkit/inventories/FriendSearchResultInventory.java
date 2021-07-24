package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.FriendHandler;
import de.sean.blockprot.bukkit.nbt.FriendModifyAction;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FriendSearchResultInventory extends BlockProtInventory {
    @Override
    int getSize() {
        return InventoryConstants.tripleLine;
    }

    @NotNull
    @Override
    String getTranslatedInventoryName() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__RESULT);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull InventoryState state) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        switch (item.getType()) {
            case BLACK_STAINED_GLASS_PANE:
                // As in the anvil inventory we cannot differentiate between
                // pressing Escape to go back, or closing it to go to the result
                // inventory, we won't return to the anvil inventory and instead
                // go right back to the FriendAddInventory.
                closeAndOpen(
                    player,
                    new FriendManageInventory().fill(player)
                );
                break;
            case PLAYER_HEAD:
            case SKELETON_SKULL:
                int index = findItemIndex(item);
                if (index >= 0 && index < state.friendResultCache.size()) {
                    OfflinePlayer friend = state.friendResultCache.get(index);
                    modifyFriendsForAction(player, friend, FriendModifyAction.ADD_FRIEND);
                    closeAndOpen(player, new FriendManageInventory().fill(player));
                }
                break;
            default:
                closeAndOpen(player, null);
                break;
        }
        event.setCancelled(true);
    }

    @Override
    public void onClose(@NotNull InventoryCloseEvent event, @NotNull InventoryState state) {

    }

    /**
     * Compare two strings by the levenshtein distance, returning a value between 0,
     * being totally unrelated strings, and 1, being identical or if both are empty.
     */
    private double compareStrings(String str1, String str2) {
        String longer = str1;
        String shorter = str2;
        if (str1.length() < str2.length()) {
            longer = str2;
            shorter = str1;
        }
        final int longerLength = longer.length();
        if (longerLength == 0) return 1.0; // They match 100% if both Strings are empty
        else return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) / (double) longerLength;
    }

    public Inventory fill(Player player, String searchQuery) {
        InventoryState state = InventoryState.get(player.getUniqueId());
        if (state == null) return inventory;

        List<OfflinePlayer> potentialFriends = Arrays.asList(Bukkit.getOfflinePlayers());

        // The already existing friends we want to add to.
        final List<String> existingFriends;
        switch (state.friendSearchState) {
            case FRIEND_SEARCH:
                final Block block = state.getBlock();
                if (block == null) return inventory;
                existingFriends = (new BlockNBTHandler(block)).getFriends()
                    .stream()
                    .map(FriendHandler::getName)
                    .collect(Collectors.toList());
                break;
            case DEFAULT_FRIEND_SEARCH:
                existingFriends = (new PlayerSettingsHandler(player)).getDefaultFriends();
                break;
            default:
                return inventory;
        }

        // We'll filter all doubled friends out of the list and add them to the current InventoryState.
        potentialFriends = potentialFriends.stream().filter((p) -> {
            // Filter all the players by search criteria.
            // If the strings are similar by 30%, the strings are considered similar (imo) and should be added.
            // If they're less than 30% similar, we should still check if it possibly contains the search criteria
            // and still add that user.
            if (p.getName() == null || p.getUniqueId().equals(player.getUniqueId())) return false;
            else if (existingFriends.contains(p.getUniqueId().toString())) return false;
            else if (compareStrings(p.getName(), searchQuery) > 0.3) return true;
            else return p.getName().contains(searchQuery);
        }).collect(Collectors.toList());
        if (state.friendSearchState == InventoryState.FriendSearchState.FRIEND_SEARCH && state.getBlock() != null) {
            // Allow integrations to additionally filter friends.
            potentialFriends = PluginIntegration.filterFriends((ArrayList<OfflinePlayer>) potentialFriends, player, state.getBlock());
        }
        state.friendResultCache.clear();
        state.friendResultCache.addAll(potentialFriends);


        // Finally, construct the inventory with all the potential friends.
        // To not delay when the inventory opens, we'll asynchronously get the items after
        // the inventory has been opened and later add them to the inventory. In the meantime,
        // we'll show the same amount of skeleton heads.
        int maxPlayers = Math.min(potentialFriends.size(), InventoryConstants.tripleLine - 2);
        for (int i = 0; i < maxPlayers; i++) {
            this.setItemStack(i, Material.SKELETON_SKULL, potentialFriends.get(i).getName());
        }
        final List<OfflinePlayer> finalPotentialFriends = potentialFriends;
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), () -> {
            // Only show the 9 * 3 - 2 most relevant players. Don't show any more.
            int playersIndex = 0;
            while (playersIndex < maxPlayers && playersIndex < finalPotentialFriends.size()) {
                // Only add to the inventory if this is not a friend (yet)
                setPlayerSkull(playersIndex, finalPotentialFriends.get(playersIndex));
                playersIndex++;
            }
        });
        setBackButton();
        return inventory;
    }
}
