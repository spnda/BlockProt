package de.sean.blockprot.bukkit.nbt;

import java.util.EnumSet;

public enum BlockAccessFlag {
    /**
     * The user is allowed to see, but not alter, the contents of the inventory.
     */
    READ,

    /**
     * The user is allowed to add and remove contents of the inventory.
     */
    WRITE;

    /**
     * Gets this enum as a bit flag.
     * @return Single bit representing this flag.
     */
    public int getFlag() {
        return 1 << ordinal();
    }

    public static EnumSet<BlockAccessFlag> parseFlags(int value) {
        EnumSet<BlockAccessFlag> flags = EnumSet.noneOf(BlockAccessFlag.class);

        String digits = Integer.toString(value, 2);
        for (int i = digits.length() - 1; i >= 0; --i) {
            char j = digits.charAt(i); // the bit to check
            if (j == '0') continue; // only add the flag if it is 1
            int k = digits.length() - 1 - i; // the ordinal of the enum, as per #getFlag
            flags.add(BlockAccessFlag.values()[k]);
        }

        return flags;
    }
}
