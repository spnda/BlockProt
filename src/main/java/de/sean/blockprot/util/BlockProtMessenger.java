package de.sean.blockprot.util;

import org.bukkit.Bukkit;

public class BlockProtMessenger {
    public static void log(String content, LogSeverity severity) {
        switch (severity) {
            case LOG: Bukkit.getLogger().info(content); break;
            case WARN: Bukkit.getLogger().warning(content); break;
            default: break;
        }
    }

    public enum LogSeverity {
        LOG, WARN;
    }
}
