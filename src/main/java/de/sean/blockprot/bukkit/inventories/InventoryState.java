package de.sean.blockprot.bukkit.inventories;

import de.sean.blockprot.bukkit.events.BlockAccessEditMenuEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Storage for the current state and location of each player's
 * interactions with this plugin's menus.
 * @since 0.1.9
 */
public final class InventoryState {
    @Nullable
    private final Block block;

    /**
     * The current state of the friend search mechanism.
     * When adding default friends this should be {@link FriendSearchState#DEFAULT_FRIEND_SEARCH},
     * which will then add the new friends to the current players NBT.
     * When adding friends for a single block this should be {@link FriendSearchState#FRIEND_SEARCH},
     * which will then add the new friends to {@link #block}'s NBT.
     * @since 0.4.7
     */
    @NotNull
    public FriendSearchState friendSearchState = FriendSearchState.FRIEND_SEARCH;

    /**
     * A local cache of offline players for this state.
     * @since 0.4.7
     */
    @NotNull
    public final ArrayList<OfflinePlayer> friendResultCache = new ArrayList<>();

    /**
     * The current index of the {@link BlockProtInventory} page.
     * @since 0.4.7
     */
    public int friendPage = 0;

    /**
     * The friend we currently want to modify with {@link FriendDetailInventory}.
     * @since 0.4.7
     */
    @Nullable
    public OfflinePlayer currentFriend = null;

    /**
     * The current cached menu access for this state.
     * @since 0.4.7
     */
    @NotNull
    public BlockAccessEditMenuEvent.MenuAccess menuAccess = BlockAccessEditMenuEvent.MenuAccess.NONE;

    /**
     * The block that backs the values of the inventory state.
     * If this inventory state is for a user settings inventory, this
     * block will be null.
     * @return The block or null, if not in a block specific inventory.
     * @since 0.2.2
     */
    @Nullable
    public final Block getBlock() {
        return this.block;
    }

    public InventoryState(@Nullable Block block) {
        this.block = block;
    }

    /**
     * HashMap containing the current InventoryState of each player.
     * The keys are the String representation of the player's UUID.
     * @since 0.4.7
     */
    private static final HashMap<String, InventoryState> players = new HashMap<>();

    /**
     * Set's {@code state} to the UUID compatible
     * String {@code player}. Overrides any previous state.
     * @since 0.4.7
     */
    public static void set(String player, InventoryState state) {
        players.put(player, state);
    }

    /**
     * Set's {@code state} to the player with UUID
     * {@code player}. Overrides any previous state.
     * @since 0.4.7
     */
    public static void set(UUID player, InventoryState state) {
        players.put(player.toString(), state);
    }

    /**
     * Get the state for the UUID {@code player}. Might
     * be null, if {@code player} is not a valid UUID or
     * there is no state for that player currently.
     * @since 0.4.7
     */
    public static InventoryState get(String player) {
        return players.get(player);
    }

    /**
     * Get the state for the UUID {@code player}. Might
     * be null, if {@code player} is not a valid UUID or
     * there is no state for that player currently.
     * @since 0.4.7
     */
    public static InventoryState get(UUID player) {
        return players.get(player.toString());
    }

    /**
     * Removes the state for {@code player}. This will not throw
     * an exception if there was no state for {@code player}.
     * @since 0.4.7
     */
    public static void remove(String player) {
        players.remove(player);
    }

    /**
     * Removes the state for {@code player}. This will not throw
     * an exception if there was no state for {@code player}.
     * @param player The UUID for the player.
     * @since 0.4.7
     */
    public static void remove(UUID player) {
        players.remove(player.toString());
    }

    /**
     * The current search state of the friend menu. Indicates
     * whether we're searching for default friends or for friends
     * to be added directly to a block.
     * @since 0.1.9
     */
    public enum FriendSearchState {
        /**
         * This search is currently for a single block.
         * @since 0.1.9
         */
        FRIEND_SEARCH,

        /**
         * This search is currently for the default friends
         * of a player.
         * @since 0.1.9
         */
        DEFAULT_FRIEND_SEARCH,
    }
}
