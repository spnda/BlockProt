/*
 * Copyright (C) 2021 - 2023 spnda
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

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.inventories.InventoryConstants;
import de.sean.blockprot.util.BlockProtUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple handler to get a player's BlockProt settings.
 *
 * @since 0.2.3
 */
public final class PlayerSettingsHandler extends FriendSupportingHandler<NBTCompound> {
    static final String LOCK_ON_PLACE_ATTRIBUTE = "splugin_lock_on_place";

    static final String DEFAULT_FRIENDS_ATTRIBUTE = "blockprot_default_friends";

    static final String PLAYER_SEARCH_HISTORY = "blockprot_player_search_history";

    /**
     * Flag saved in NBT to check if the player has ever interacted with a
     * menu from BlockProt and if any hints should be sent to them.
     */
    static final String PLAYER_HAS_INTERACTED_WITH_MENU = "blockprot_player_has_interacted_with_menu";

    private static final int MAX_HISTORY_SIZE = InventoryConstants.tripleLine - 2;

    /**
     * The player that this settings handler is getting values
     * for.
     *
     * @since 0.2.3
     */
    public final Player player;

    /**
     * Create a new settings handler.
     *
     * @param player The player to get the settings for.
     * @since 0.2.3
     */
    public PlayerSettingsHandler(@NotNull final Player player) {
        super(DEFAULT_FRIENDS_ATTRIBUTE);
        this.player = player;

        this.container = new NBTEntity(player).getPersistentDataContainer();
    }

    /**
     * Check if the player wants their blocks to be locked when
     * placed.
     *
     * @return Will return the default setting from the config, or the
     * value the player has set it to.
     * @since 0.2.3
     */
    public boolean getLockOnPlace() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(LOCK_ON_PLACE_ATTRIBUTE))
            return BlockProt.getDefaultConfig().lockOnPlaceByDefault();
        return container.getBoolean(LOCK_ON_PLACE_ATTRIBUTE);
    }

    /**
     * Set the value of the lock on place setting. If true, the
     * player wants to lock any block right after placing it.
     *
     * @param lockOnPlace The boolean value to set it to.
     * @since 0.2.3
     */
    public void setLockOnPlace(final boolean lockOnPlace) {
        container.setBoolean(LOCK_ON_PLACE_ATTRIBUTE, lockOnPlace);
    }

    /**
     * We are switching to a similar system that {@link BlockNBTHandler}
     * uses. To retain compatibility and upgradability with older versions
     * we will try to remap the previous data to the new data structure.
     * 
     * @since 1.0.0
     */
    @Override
    protected void preFriendReadCallback() {
        if (container.hasKey(DEFAULT_FRIENDS_ATTRIBUTE)
            && container.getType(DEFAULT_FRIENDS_ATTRIBUTE) == NBTType.NBTTagString) {
            final List<String> originalList = BlockProtUtil
                .parseStringList(container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
            
            container.removeKey(DEFAULT_FRIENDS_ATTRIBUTE); // We have to remove the string to then add the compound.
            container.addCompound(DEFAULT_FRIENDS_ATTRIBUTE);
            originalList.forEach(this::addFriend);
        }
    }

    /**
     * Get the current search history for this player.
     * 
     * @return A list of UUIDs for each player this player has
     * searched for.
     */
    public List<String> getSearchHistory() {
        if (!container.hasKey(PLAYER_SEARCH_HISTORY)) return new ArrayList<>();
        else {
            return BlockProtUtil
                .parseStringList(container.getString(PLAYER_SEARCH_HISTORY));
        }
    }

    /**
     * Add a player to the search history.
     * 
     * @param player The player to add.
     */
    public void addPlayerToSearchHistory(@NotNull final OfflinePlayer player) {
        this.addPlayerToSearchHistory(player.getUniqueId().toString());
    }

    /**
     * Add a player to the search history.
     * 
     * @param playerUuid The player UUID to add.
     */
    public void addPlayerToSearchHistory(@NotNull final String playerUuid) {
        List<String> history = getSearchHistory();
        if (!history.contains(playerUuid)) {
            // We want the list to not be bigger than MAX_HISTORY_SIZE,
            // therefore we remove the first entry if we would exceed that size.
            if (history.size() == MAX_HISTORY_SIZE) {
                history.remove(0);
            }
            history.add(playerUuid);
            container.setString(PLAYER_SEARCH_HISTORY, history.toString());
        }
    }

    /**
     * Get whether this player has interacted with any of the plugin's
     * menus before.
     * @return true if the player has interacted with a menu at least once.
     */
    public boolean hasPlayerInteractedWithMenu() {
        if (!container.hasKey(PLAYER_HAS_INTERACTED_WITH_MENU)) {
            return false;
        } else {
            return container.getBoolean(PLAYER_HAS_INTERACTED_WITH_MENU);
        }
    }

    /**
     * Sets whether this player has interacted with any of the plugin's
     * menus before. Toggleable in order to allow re-enabling of hints.
     */
    public void setHasPlayerInteractedWithMenu(boolean bool) {
        container.setBoolean(PLAYER_HAS_INTERACTED_WITH_MENU, bool);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeHandler(@NotNull NBTHandler<?> handler) {
        if (!(handler instanceof final PlayerSettingsHandler playerSettingsHandler)) return;
        this.setLockOnPlace(playerSettingsHandler.getLockOnPlace());
        this.container.setString(DEFAULT_FRIENDS_ATTRIBUTE,
            playerSettingsHandler.container.getString(DEFAULT_FRIENDS_ATTRIBUTE));
    }
}
