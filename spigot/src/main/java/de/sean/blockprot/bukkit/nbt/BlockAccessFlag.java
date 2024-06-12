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

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * @since 0.2.3
 */
public enum BlockAccessFlag {
    /**
     * The user is allowed to see, but not alter, the contents of the inventory.
     *
     * @since 0.2.3
     */
    READ(TranslationKey.INVENTORIES__FRIENDS__PERMISSION__READ),

    /**
     * The user is allowed to add and remove contents of the inventory.
     *
     * @since 0.2.3
     */
    WRITE(TranslationKey.INVENTORIES__FRIENDS__PERMISSION__WRITE),

    /**
     * The user is allowed to manage redstone settings and manage friends.
     * 
     * @since 1.0.0
     */
    MANAGER(TranslationKey.INVENTORIES__FRIENDS__PERMISSION__MANAGER);

    /**
     * The translation key used for the description of this flag.
     *
     * @since 0.2.3
     */
    @NotNull
    private final TranslationKey descriptionKey;

    /**
     * @since 0.2.3
     */
    BlockAccessFlag(@NotNull final TranslationKey description) {
        this.descriptionKey = description;
    }

    /**
     * Parse flags from a single int value. This is done at a binary level. They are
     * parsed from their value from {@link BlockAccessFlag#getFlag()}.
     *
     * @param value The integer to parse from.
     * @return A {@link EnumSet} with all flags that were parsed from
     * given integer.
     * @since 0.2.3
     */
    public static EnumSet<BlockAccessFlag> parseFlags(final int value) {
        EnumSet<BlockAccessFlag> flags = EnumSet.noneOf(BlockAccessFlag.class);

        final BlockAccessFlag[] values = BlockAccessFlag.values();
        String digits = Integer.toString(value, 2);
        for (int i = digits.length() - 1; i >= 0; --i) {
            char j = digits.charAt(i); // the bit to check
            if (j == '0') {
                // only add the flag if it is 1
                continue;
            }
            int k = digits.length() - 1 - i; // the ordinal of the enum, as per #getFlag
            if (k < values.length) {
                flags.add(values[k]);
            }
        }

        return flags;
    }

    /**
     * Gets a user-friendly name of the permissions item title, that should be used together
     * with {@link #accumulateAccessFlagLore(EnumSet)}.
     *
     * @return Simple title string.
     */
    public static String toBaseString() {
        return Translator.get(TranslationKey.INVENTORIES__FRIENDS__PERMISSIONS);
    }

    /**
     * Accumulate the access flag details into a list of strings, where each entry
     * represents a line. Used for item lore in the GUI.
     *
     * @param flags A {@link EnumSet} of flags to convert.
     * @return List of strings used for item lore.
     * @since 0.3.0
     */
    @NotNull
    public static List<String> accumulateAccessFlagLore(@NotNull final EnumSet<BlockAccessFlag> flags) {
        if (flags.isEmpty()) return Collections.singletonList("No access");
        ArrayList<String> ret = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (BlockAccessFlag flag : flags) {
            builder.setLength(0);
            builder
                .append(ChatColor.RESET)
                .append(ChatColor.YELLOW)
                .append(flag.getDescription());
            ret.add(builder.toString());
        }
        return ret;
    }

    /**
     * Get the translated description of this flag. It uses {@link Translator#get(TranslationKey)}
     * using the {@link #descriptionKey} to get the corresponding value.
     *
     * @return Translated description.
     * @since 0.2.3
     */
    @NotNull
    public String getDescription() {
        return Translator.get(descriptionKey);
    }

    /**
     * Gets this enum as a bit flag.
     *
     * @return Single bit representing this flag.
     * @since 0.2.3
     */
    public int getFlag() {
        return 1 << ordinal();
    }
}
