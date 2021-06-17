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
package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.TranslationKey;
import de.sean.blockprot.Translator;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum BlockAccessFlag {
    /**
     * The user is allowed to see, but not alter, the contents of the inventory.
     */
    READ(TranslationKey.INVENTORIES__FRIENDS__PERMISSIONS__READ),

    /**
     * The user is allowed to add and remove contents of the inventory.
     */
    WRITE(TranslationKey.INVENTORIES__FRIENDS__PERMISSIONS__WRITE);

    @NotNull
    private final TranslationKey descriptionKey;

    BlockAccessFlag(@NotNull final TranslationKey description) {
        this.descriptionKey = description;
    }

    /**
     * Get the translated description of this flag. It uses {@link Translator#get(TranslationKey)}
     * using the {@link #descriptionKey} to get the corresponding value.
     *
     * @return Translated description.
     */
    @NotNull
    public String getDescription() {
        return Translator.get(descriptionKey);
    }

    /**
     * Gets this enum as a bit flag.
     *
     * @return Single bit representing this flag.
     */
    public int getFlag() {
        return 1 << ordinal();
    }

    /**
     * Parse flags from a single int value. This is done at a binary level. They are
     * parsed from their value from {@link BlockAccessFlag#getFlag()}.
     *
     * @param value The integer to parse from.
     * @return A {@link EnumSet<BlockAccessFlag>} with all flags that were parsed from
     * given integer.
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

    @NotNull
    public static String accessFlagToString(@NotNull final EnumSet<BlockAccessFlag> flags) {
        if (flags.isEmpty()) return "No access";
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (BlockAccessFlag flag : flags) {
            String flagStr = flag.toString();
            builder
                .append(ChatColor.ITALIC)
                .append(flagStr.substring(0, 1).toUpperCase(Locale.ENGLISH)) // Uppercase first letter.
                .append(flagStr.substring(1).toLowerCase(Locale.ENGLISH));
            if (i < (flags.size() - 1)) {
                builder.append(ChatColor.RESET).append(", ");
            }
            i++;
        }
        return builder.toString();
    }

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
}
