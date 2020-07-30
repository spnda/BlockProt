package de.sean.splugin.spigot.events;

import de.sean.splugin.App;
import de.sean.splugin.util.SUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;

import java.util.Arrays;
import java.util.List;

public class BroadcastEvent implements Listener {
    @EventHandler
    public static void onBroadcastMessage(final BroadcastMessageEvent event) {
        List<String> possibleMorningMessages = Arrays.asList("&eThe night has been skipped.", "&eAhhh, finally morning.", "&eArghh, it's so bright outside.");
        if (possibleMorningMessages.contains(event.getMessage())) {
            // It's now morning. Send a message to discord.
            JDA jda = App.getInstance().getDiscordInstance();
            if (jda != null) {
                Guild guild = jda.getGuildById(SUtil.GUILD_ID);
                if (guild == null) return;
                TextChannel channel = guild.getTextChannelById(SUtil.CHANNEL_ID);
                try {
                    if (channel != null) channel.sendMessage("Es ist nun morgen.").queue();
                } catch (Exception e) {
                    App.getInstance().getLogger().severe("Couldn't send message to Discord: " + e.toString());
                }
            }
        }
    }
}
