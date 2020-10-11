package de.sean.splugin.discord;

import de.sean.splugin.SPlugin;
import de.sean.splugin.util.SUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SHandler extends ListenerAdapter implements EventListener {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        SPlugin.instance.getLogger().info("Discord has started!");
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        // Maybe this will be useful sometime...
    }

    @Override
    public void onGenericEvent(final @NotNull GenericEvent event) {
        // This is purely a thing for myself. Whenever the IP changes my DNS record gets changed.
        // Can be ignored by anyone else.
        if (event instanceof ReconnectedEvent) {
            SPlugin.instance.updateIP();
        }
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        final String[] message = event.getMessage().getContentRaw().split(" ");
        if (event.getChannelType().equals(ChannelType.PRIVATE)) {
            if (message[0].equals("?msg")) {
                if (message.length < 3) {
                    event.getChannel().sendMessage("Not enough arguments. Usage: `?msg <Player Name> <Message>`").queue();
                } else {
                    final User user = event.getAuthor();
                    final Player player = Bukkit.getPlayer(message[1]);
                    if (player != null) player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + user.getName() + " whispers to you: " + SUtil.concatArrayRange(message, 2, message.length));
                }
            }
        }

        EmbedBuilder eb;
        switch (message[0]) {
            case "?help":
                eb = new EmbedBuilder();
                eb.setColor(SUtil.randomColor());
                eb.setTitle("Server Help", null);
                eb.setDescription("This bot links a minecraft server with a discord bot. https://github.com/spnda/SPlugin");
                eb.addField("Private Messages", "Using `?msg` you can whisper to any player currently online on the server.", false);
                eb.addField("Online Players", "`?players` will give you a neat list of all online players.", false);
                event.getChannel().sendMessage(eb.build()).queue();
                break;
            case "?players":
                eb = new EmbedBuilder();
                eb.setColor(SUtil.randomColor());
                eb.setTitle("Online Players", null);
                final List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                eb.setDescription("There are " + players.size() + " / " + Bukkit.getMaxPlayers() + " players online.");
                final StringBuilder playerList = new StringBuilder();
                for (Player player : players) {
                    playerList.append(player.getDisplayName().replaceAll("§[a-z]", "")).append("\n");
                }
                if (playerList.length() > 0) eb.addField("Players online", playerList.toString(), false);
                event.getChannel().sendMessage(eb.build()).queue();
                break;
            default:
                if (DiscordUtil.channels.get(event.getGuild()) == null) break;
                if (!DiscordUtil.channels.get(event.getGuild()).equals(event.getTextChannel())) break;
                if (event.getAuthor().isBot()) break; // Ignore all bots.
                String msg = event.getMessage().getContentStripped();
                if (event.getMessage().getAttachments().size() > 0) msg += msg.length() > 0 ? " " : "" + "[file]"; // Show a nice indicator that the person has sent a image.
                Bukkit.broadcastMessage(ChatColor.BLUE + "Discord" + ChatColor.GRAY + " | " + ChatColor.RESET + event.getAuthor().getName() + ": " + msg);
                break;
        }
    }
}
