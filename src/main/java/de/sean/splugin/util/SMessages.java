package de.sean.splugin.util;

/* SPlugin */
import de.sean.splugin.App;

/* Java */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

/* Spigot */
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SMessages {
    public static void markPlayerAFK(@NotNull Player player) {
        Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " is now AFK.");
        player.setDisplayName(player.getDisplayName() + " (AFK)");
        player.setPlayerListName(player.getPlayerListName() + " (AFK)");
    }

    public static void unmarkPlayerAFK(@NotNull Player player) {
        player.setPlayerListName(player.getPlayerListName().replace(" (AFK)", ""));
        player.setDisplayName(player.getDisplayName().replace(" (AFK)", ""));
        Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " is no more AFK.");
    }
    
    public static String getRandomMessage(final String messageList) {
        final List<?> messages = App.getInstance().getConfig().getList(messageList, new ArrayList<>());
        if (messages != null && messages.size() > 0) {
            if (messages.size() == 1) return (String) messages.get(0);
            final int index = new Random().nextInt(Math.max(0, messages.size()));
            return (String) messages.get(index);
        }
        // If no messages we're defined, return the default ones
        switch (messageList) {
            case "Messages.Join": return "[player] has joined the server!";
            case "Messages.Leave": return "[player] has left the server!";
            case "Messages.Death": return "[message]...";
            default: return "";
        }
    }
    
    public static void sendPrivateMessage(final String message, final @NotNull Player player) {
        player.sendMessage(message);
    }

    public static void sendGlobalMessage(final String message) {
        Bukkit.broadcastMessage(message);
    }

    public static void sendGlobalActionBarMessage(final String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBarMessage(player, message);
        }
    }

    public static void sendActionBarMessage(final @NotNull Player player, final String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static void sendGlobalTitleMessage(final String title, final String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitleMessage(player, title, subtitle);
        }
    }

    public static void sendTitleMessage(final @NotNull Player player, final String title, final String subtitle) {
        player.sendTitle(title, subtitle, 10, 80, 20);
    }
}
