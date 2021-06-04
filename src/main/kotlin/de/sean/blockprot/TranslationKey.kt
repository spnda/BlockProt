/*
 * This file is part of BlockProt, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 spnda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
