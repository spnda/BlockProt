package de.sean.splugin.discord;

import de.sean.splugin.App;
import de.sean.splugin.util.SUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.configuration.file.FileConfiguration;

import javax.security.auth.login.LoginException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public class DiscordUtil {
    private JDA jda;
    public static final Map<Guild, TextChannel> channels = new HashMap<>();
    public final boolean joinMessage;
    public final boolean leaveMessage;

    public DiscordUtil(FileConfiguration config) throws NullPointerException {
        Map<String, Object> discord = config.getConfigurationSection("Discord").getValues(true);
        System.out.println(discord);
        final String token = config.getString("Discord.Token");
        this.joinMessage = config.getBoolean("Discord.JoinMessage");
        this.leaveMessage = config.getBoolean("Discord.LeaveMessage");
        // Only initialize discord stuff if a guild, channel and token are present.
        if (token != null) {
            JDABuilder builder = new JDABuilder(token);
            try {
                builder.setActivity(Activity.playing("Minecraft"));
                jda = builder.build();
                jda.awaitReady();
                App.getInstance().getLogger().info("Discord has started successfully!");

                // Add event listeners for discord
                jda.addEventListener(new SHandler());
            } catch (LoginException | InterruptedException e) {
                e.printStackTrace();
            }

            Map<String, Object> channelsConfig = config.getConfigurationSection("Discord.Channels").getValues(true);
            System.out.println(channelsConfig);
            for (Map.Entry<String, Object> pair : channelsConfig.entrySet()) {
                System.out.println(pair.getKey() + ": " + pair.getValue());
                Guild guild = jda.getGuildById(pair.getKey());
                if (guild != null) {
                    TextChannel channel = guild.getTextChannelById(pair.getValue().toString());
                    if (channel != null) channels.put(guild, channel);
                }
            }
        }
    }

    public void sendMessage(String message) {
        for (Map.Entry<Guild, TextChannel> pair : channels.entrySet()) {
            pair.getValue().sendMessage(message).queue();
        }
    }
}
