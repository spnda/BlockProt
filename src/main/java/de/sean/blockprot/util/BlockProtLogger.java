package de.sean.blockprot.util;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlockProtLogger {
    public static void log(String content) {
        log(content, LogSeverity.WARN);
    }

    public static void log(String content, LogSeverity severity) {
        switch (severity) {
            case LOG: Bukkit.getLogger().info(content); break;
            case WARN: Bukkit.getLogger().warning(content); break;
            default: break;
        }
    }

    public static void sendMessage(String content) {
        try {
            Class<?> advComponent = Class.forName("net.kyori.adventure.text.Component");
            try {
                Method textMethod = advComponent.getMethod("text", String.class);
                Method sendMessageMethod = Server.class.getMethod("sendMessage", advComponent);
                sendMessageMethod.invoke(Bukkit.getServer(), textMethod.invoke(null, content));
            } catch (NoSuchMethodError | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("Adventure is present but couldn't find proper sendMessage method.");
            }
        } catch (ClassNotFoundException e) {
            // Adventure is not present, so we will have to use the default Bukkit/Bungee methods.
            // Always send the message as gold text
            Bukkit.spigot().broadcast(TextComponent.fromLegacyText("§6" + content));
        }
    }

    public enum LogSeverity {
        LOG, WARN;
    }
}
