package de.sean.blockprot

import java.util.*

/**
 * A translation key for a single string in a translations_xx.yml file.
 * Double underscores are replaced with dots when querying for YAML keys.
 */
enum class TranslationKey {
    INVENTORIES__BACK,
    INVENTORIES__LAST_PAGE,
    INVENTORIES__NEXT_PAGE,
    INVENTORIES__BLOCK_LOCK,
    INVENTORIES__BLOCK_INFO,
    INVENTORIES__USER_SETTINGS,
    INVENTORIES__LOCK,
    INVENTORIES__UNLOCK,
    INVENTORIES__REDSTONE__ALLOWED,
    INVENTORIES__REDSTONE__DISALLOWED,
    INVENTORIES__REDSTONE__DISALLOW,
    INVENTORIES__REDSTONE__ALLOW,
    INVENTORIES__LOCK_ON_PLACE__ACTIVATE,
    INVENTORIES__LOCK_ON_PLACE__DEACTIVATE,
    INVENTORIES__FRIENDS__MANAGE,
    INVENTORIES__FRIENDS__EDIT,
    INVENTORIES__FRIENDS__REMOVE,
    INVENTORIES__FRIENDS__SEARCH,
    INVENTORIES__FRIENDS__RESULT,

    MESSAGES__PERMISSION_GRANTED,
    MESSAGES__UNLOCKED,
    MESSAGES__NO_PERMISSION,
    MESSAGES__FRIEND_ADDED,
    MESSAGES__FRIEND_REMOVED,
    MESSAGES__FRIEND_ALREADY_ADDED,
    MESSAGES__FRIEND_CANT_BE_REMOVED,
    MESSAGES__REDSTONE_ADDED,
    MESSAGES__REDSTONE_REMOVED;

    /**
     * Get the string representation of this [TranslationKey] as a valid
     * YAML key. [TranslationKey.INVENTORIES__BACK] becomes "inventories.back",
     * whereas [TranslationKey.INVENTORIES__BLOCK_LOCK] becomes "inventories.block_lock".
     */
    override fun toString(): String {
        return name.replace("__", ".").lowercase(Locale.ENGLISH)
    }
}
