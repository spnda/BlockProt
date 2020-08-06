package de.sean.splugin.discord;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SUtil;

/* Java */
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/* Spigot */
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/* Discord */
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;

public class SHandler extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        App.getInstance().getLogger().info("Discord has started!");
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        // Maybe this will be useful sometime...
    }

    @SubscribeEvent
    public void onGenericEvent(final @NotNull GenericEvent event) {
        // This is purely a thing for myself. Whenever the IP changes my DNS record gets changed.
        // Can be ignored by anyone else.
        if (event instanceof ReconnectedEvent) {
            App.getInstance().updateIP();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (event.getChannelType().equals(ChannelType.PRIVATE)) {
            if (message[0].equals("?msg")) {
                if (message.length < 3) {
                    event.getChannel().sendMessage("Nicht genug Argumente. Nutzung: `?msg <MC Name> <Nachricht>`").queue();
                } else {
                    User user = event.getAuthor();
                    Player player = Bukkit.getPlayer(message[1]);
                    if (player != null) player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + user.getName() + " whispers to you: " + SUtil.concatArrayRange(message, 2, message.length));
                }
            }
            return;
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
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                eb.setDescription("There are " + players.size() + " / " + Bukkit.getMaxPlayers() + " players online.");
                StringBuilder playerList = new StringBuilder();
                for (Player player : players) {
                    playerList.append(player.getDisplayName().replaceAll("§[a-z]", "")).append("\n");
                }
                eb.addField("Players online", playerList.toString(), false);
                event.getChannel().sendMessage(eb.build()).queue();
                break;
            case "?test":
                onGenericEvent(new ReconnectedEvent(App.getInstance().getDiscordInstance(), 0L));
                break;
            default:
                if (!event.getChannel().getId().equals(SUtil.CHANNEL_ID)) return;
                if (event.getAuthor().isBot()) return; // Ignore all bots.
                String divider = ChatColor.GRAY + " | " + ChatColor.RESET;
                String msg = event.getMessage().getContentStripped();
                if (msg.replaceAll(" ", "").isEmpty()) return; // Usually this is a image/embed, can't send these.
                Bukkit.broadcastMessage(ChatColor.BLUE + "Discord" + divider + event.getAuthor().getName() + ": " + msg);
                break;
        }
    }
}
