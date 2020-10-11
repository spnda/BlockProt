package de.sean.splugin.util;

import org.bukkit.block.Block;

import java.util.HashMap;

public class SLockUtil {
    public static final String OWNER_ATTRIBUTE = "splugin_owner";
    public static final String LOCK_ATTRIBUTE = "splugin_lock";
    public static final String REDSTONE_ATTRIBUTE = "splugin_lock_redstone";

    public static final HashMap<String, Block> lock = new HashMap<>();
}
