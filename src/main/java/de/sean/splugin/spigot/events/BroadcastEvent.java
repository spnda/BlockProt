package de.sean.splugin.spigot.events;

/* SPLugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.Arrays;
import java.util.List;

/* Spigot */
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;

/* Discord */
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class BroadcastEvent implements Listener {
    @EventHandler
    public static void onBroadcastMessage(final BroadcastMessageEvent event) {
        List<String> possibleMorningMessages = Arrays.asList("&eThe night has been skipped.", "&eAhhh, finally morning.", "&eArghh, it's so bright outside.");
        if (possibleMorningMessages.contains(event.getMessage())) {
            // It's now morning. Send a message to discord.
            App.getInstance().getDiscordUtil().sendMessage(event.getMessage());
        }
    }
}
