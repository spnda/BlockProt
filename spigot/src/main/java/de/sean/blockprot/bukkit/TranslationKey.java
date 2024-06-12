/*
 * Copyright (C) 2021 - 2024 spnda
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

import java.util.Locale;

/**
 * A translation key for a single string in a translations_xx.yml file.
 * Double underscores are replaced with dots when querying for YAML keys.
 *
 * @since 0.1.10
 */
public enum TranslationKey {
    ENABLED,
    DISABLED,
    INVENTORIES__BACK,
    INVENTORIES__LAST_PAGE,
    INVENTORIES__NEXT_PAGE,
    INVENTORIES__BLOCK_LOCK,
    INVENTORIES__BLOCK_INFO,
    INVENTORIES__USER_SETTINGS,
    INVENTORIES__LOCK,
    INVENTORIES__UNLOCK,
    INVENTORIES__LOCK_ON_PLACE,
    INVENTORIES__COPY_CONFIGURATION,
    INVENTORIES__PASTE_CONFIGURATION,
    INVENTORIES__SET_BLOCK_NAME,
    INVENTORIES__INSPECT_CONTENTS,
    INVENTORIES__REDSTONE__SETTINGS,
    INVENTORIES__REDSTONE__REDSTONE_PROTECTION,
    INVENTORIES__REDSTONE__HOPPER_PROTECTION,
    INVENTORIES__REDSTONE__PISTON_PROTECTION,
    INVENTORIES__REDSTONE__ENABLE_ALL,
    INVENTORIES__REDSTONE__DISABLE_ALL,
    INVENTORIES__FRIENDS__MANAGE,
    INVENTORIES__FRIENDS__SEARCH,
    INVENTORIES__FRIENDS__RESULT,
    INVENTORIES__FRIENDS__SEARCH_HISTORY,
    INVENTORIES__FRIENDS__EDIT,
    INVENTORIES__FRIENDS__REMOVE,
    INVENTORIES__FRIENDS__PERMISSIONS,
    INVENTORIES__FRIENDS__PERMISSION__READ,
    INVENTORIES__FRIENDS__PERMISSION__WRITE,
    INVENTORIES__FRIENDS__PERMISSION__MANAGER,
    INVENTORIES__FRIENDS__THE_PUBLIC,
    INVENTORIES__FRIENDS__THE_PUBLIC_DESC,
    INVENTORIES__FRIENDS__MAKE_PUBLIC,
    INVENTORIES__STATISTICS__STATISTICS,
    INVENTORIES__STATISTICS__PLAYER_STATISTICS,
    INVENTORIES__STATISTICS__GLOBAL_STATISTICS,
    INVENTORIES__STATISTICS__CONTAINER_COUNT,
    INVENTORIES__STATISTICS__YOUR_BLOCKS,

    MESSAGES__PERMISSION_GRANTED,
    MESSAGES__UNLOCKED,
    MESSAGES__NO_PERMISSION,
    MESSAGES__FRIEND_ADDED,
    MESSAGES__FRIEND_REMOVED,
    MESSAGES__FRIEND_ALREADY_ADDED,
    MESSAGES__FRIEND_CANT_BE_REMOVED,
    MESSAGES__REDSTONE_ADDED,
    MESSAGES__REDSTONE_REMOVED,
    MESSAGES__EXCEEDED_MAX_BLOCK_COUNT,
    MESSAGES__LOCK_HINT,
    MESSAGES__HINT_HOVER_TEXT,
    MESSAGES__DISABLED_HINTS,

    INTEGRATIONS__LANDS__PROTECT_CONTAINERS_FLAG_NAME,
    INTEGRATIONS__LANDS__PROTECT_CONTAINERS_DESC,
    INTEGRATIONS__LANDS__REQUIRE_PROTECT_FOR_FRIENDS_FLAG_NAME,
    INTEGRATIONS__LANDS__REQUIRE_PROTECT_FOR_FRIENDS_DESC;

    /**
     * Get the string representation of this {@link TranslationKey} as
     * a valid YAML key. {@link TranslationKey#INVENTORIES__BACK} becomes
     * "inventories.back", whereas {@link TranslationKey#INVENTORIES__BLOCK_LOCK}
     * becomes "inventories.block_lock".
     */
    @Override
    public String toString() {
        return name().replace("__", ".").toLowerCase(Locale.ENGLISH);
    }
}
