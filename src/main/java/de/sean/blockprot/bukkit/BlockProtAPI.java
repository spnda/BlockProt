package de.sean.blockprot.bukkit;

import de.sean.blockprot.bukkit.events.BlockAccessEditMenuEvent;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.inventories.BlockLockInventory;
import de.sean.blockprot.bukkit.inventories.InventoryState;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.nbt.PlayerSettingsHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * BlockProt's class for external API methods.
 *
 * @author spnda
 * @since 0.4.7
 */
public final class BlockProtAPI {
    @Nullable
    static BlockProtAPI instance;

    private final BlockProt blockProt;

    BlockProtAPI(BlockProt blockProt) {
        this.blockProt = blockProt;
        instance = this;
    }

    /**
     * Get the current instance of this API. Remember to
     * softdepend this plugin, otherwise this API will not be
     * initialized and this will return null.
     *
     * @return The instance or null if not initialized
     * @since 0.4.7
     */
    @Nullable
    public static BlockProtAPI getInstance() {
        return instance;
    }

    /**
     * Registers a integration. This automatically calls {@link PluginIntegration#load()}
     * to load the plugin and also adds them to a internal list (accessible through
     * {@link #getIntegrations()}) which is used for friend handling. This does not ensure
     * that there are no duplicates of a integration, so please beware to only register your
     * integration once.
     *
     * @param integration The integration to register.
     * @since 0.4.7
     */
    public void registerIntegration(@NotNull final PluginIntegration integration) {
        this.blockProt.registerIntegration(integration);
    }

    /**
     * Get a list of all integrations that have been registered using
     * {@link #registerIntegration(PluginIntegration)}. There might possibly
     * be duplicates of some integrations, if the author of those registered them
     * more than once.
     *
     * @return A unmodifiable list of all registered integrations.
     * @since 0.4.7
     */
    @NotNull
    public List<PluginIntegration> getIntegrations() {
        return this.blockProt.getIntegrations();
    }

    /**
     * Get the handler for given blocks. This is used to get information
     * about the owner, the friends and the redstone protection.
     *
     * @param block The block to get the handler for.
     * @return The {@link BlockNBTHandler} for the given block.
     * @since 0.4.7
     */
    @NotNull
    public BlockNBTHandler getBlockHandler(@NotNull final Block block) {
        return new BlockNBTHandler(block);
    }

    /**
     * Get the player settings handler for given player. This is used
     * to retrieve default friends or other globally applicable settings
     * for the player.
     *
     * @param player The player.
     * @return The {@link PlayerSettingsHandler} for the given player.
     * @since 0.4.7
     */
    @NotNull
    public PlayerSettingsHandler getPlayerSettings(@NotNull final Player player) {
        return new PlayerSettingsHandler(player);
    }

    /**
     * Get the lock inventory for given block and player. This call
     * triggers the {@link BlockAccessEditMenuEvent} event and checks
     * if it succeeded and what permissions the player has and bases the
     * inventory on that information.
     *
     * @param block  The block the {@code player} is trying to access.
     * @param player The player.
     * @return The inventory or null, if the request was denied, possibly
     * due to permissions.
     * @since 0.4.7
     */
    @Nullable
    public Inventory getLockInventoryForBlock(@NotNull final Block block, @NotNull final Player player) {
        final BlockAccessEditMenuEvent event = new BlockAccessEditMenuEvent(block, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getAccess() == BlockAccessEditMenuEvent.MenuAccess.NONE) {
            return null;
        }

        final BlockNBTHandler handler = new BlockNBTHandler(block);
        if (player.hasPermission(BlockNBTHandler.PERMISSION_ADMIN)) {
            event.setAccess(BlockAccessEditMenuEvent.MenuAccess.ADMIN);
        } else if (player.hasPermission(BlockNBTHandler.PERMISSION_INFO) || player.isOp()) {
            event.setAccess(BlockAccessEditMenuEvent.MenuAccess.INFO);
        } else if (handler.isNotProtected() || handler.isOwner(player.getUniqueId().toString())) {
            event.setAccess(BlockAccessEditMenuEvent.MenuAccess.NORMAL);
        } else {
            event.setAccess(BlockAccessEditMenuEvent.MenuAccess.NONE);
        }

        if (event.getAccess() == BlockAccessEditMenuEvent.MenuAccess.NONE) {
            return null;
        } else {
            InventoryState state = new InventoryState(block);
            state.menuAccess = event.getAccess();
            state.friendSearchState = InventoryState.FriendSearchState.FRIEND_SEARCH;
            InventoryState.set(player.getUniqueId(), state);

            return new BlockLockInventory().fill(player, block.getType(), handler);
        }
    }
}
