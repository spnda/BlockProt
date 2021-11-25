/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.bukkit;

import de.sean.blockprot.bukkit.events.BlockAccessMenuEvent;
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
     * Registers a integration. This automatically calls {@link PluginIntegration#enable()}
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
     * triggers the {@link BlockAccessMenuEvent} event and checks
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
        final BlockAccessMenuEvent event = new BlockAccessMenuEvent(block, player);
        final String playerUuid = player.getUniqueId().toString();

        final BlockNBTHandler handler = new BlockNBTHandler(block);
        if (player.hasPermission(BlockNBTHandler.PERMISSION_ADMIN)) {
            event.addPermissions(
                BlockAccessMenuEvent.MenuPermission.LOCK,
                BlockAccessMenuEvent.MenuPermission.INFO);
        } else if (player.hasPermission(BlockNBTHandler.PERMISSION_INFO)) {
            event.addPermission(BlockAccessMenuEvent.MenuPermission.INFO);
        }

        if (handler.isOwner(playerUuid)) {
            event.addPermissions(
                BlockAccessMenuEvent.MenuPermission.LOCK,
                BlockAccessMenuEvent.MenuPermission.INFO,
                BlockAccessMenuEvent.MenuPermission.MANAGER);
        } else if (handler.isNotProtected()) {
            event.addPermission(BlockAccessMenuEvent.MenuPermission.LOCK);
        } else if (handler.getFriend(playerUuid).isPresent()
            && handler.getFriend(playerUuid).get().isManager()) {
            event.addPermission(BlockAccessMenuEvent.MenuPermission.MANAGER);
        }

        // Call the event and let the listeners remove/add more permissions.
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || event.getPermissions().isEmpty()) {
            return null;
        }

        InventoryState state = new InventoryState(block);
        state.menuPermissions = event.getPermissions();
        state.friendSearchState = InventoryState.FriendSearchState.FRIEND_SEARCH;
        InventoryState.set(player.getUniqueId(), state);

        return new BlockLockInventory().fill(player, block.getType(), handler);
    }
}
