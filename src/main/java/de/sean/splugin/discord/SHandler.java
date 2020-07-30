package de.sean.splugin.discord;

import de.sean.splugin.App;
import de.sean.splugin.util.SUtil;

/* Spigot */
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

/* Discord */
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        if (event instanceof ReconnectedEvent) {
            new Thread(() -> {
                System.out.println("API has reconnected!");
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "node .");
                processBuilder.directory(new File("./freenom-update"));
                System.out.println(processBuilder.directory());
                try {
                    Process process = processBuilder.start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                    }
                    process.waitFor();
                    bufferedReader.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (event.getChannelType().equals(ChannelType.PRIVATE)) {
            if (message[0].equals("?msg")) {
                if (message.length < 3) {
                    event.getChannel().sendMessage("Nicht genug Argumente. Nutzung: `?msg <MC Name> <Nachricht>`");
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
                eb.setTitle("9D Server Hilfe", null);
                eb.setDescription("Dieser Bot bindet den 9D Minecraft Server mit dem 9D Discord Server. Bot und Plugin geschrieben von Sean.");
                eb.addField("Privat Nachrichten", "Ihr k\u00F6nnt mit `?msg`, wenn ihr im Privatchat mit diesem Bot seid, private Nachrichten an Spieler auf dem Minecraft Server schicken.", false);
                event.getChannel().sendMessage(eb.build()).queue();
                break;
            case "?players":
                eb = new EmbedBuilder();
                eb.setColor(SUtil.randomColor());
                eb.setTitle("9D Online Spieler", null);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                eb.setDescription("Es sind " + players.size() + " von maximal " + Bukkit.getMaxPlayers() + " Spielern online.");
                StringBuilder playerList = new StringBuilder();
                for (Player player : players) {
                    playerList.append(player.getDisplayName().replaceAll("§[a-z]", "")).append("\n");
                }
                eb.addField("Spieler online", playerList.toString(), false);
                event.getChannel().sendMessage(eb.build()).queue();
                break;
            case "?test":
                onGenericEvent(new ReconnectedEvent(App.getInstance().getDiscordInstance(), 0L));
                break;
            default:
                if (!event.getChannel().getId().equals(SUtil.channelID)) return; // mc-server channel. UND KEIN ANDERER!
                if (event.getAuthor().isBot()) return; // Ignore all bots.
                String divider = ChatColor.GRAY + " | " + ChatColor.RESET;
                String msg = event.getMessage().getContentStripped();
                if (msg.replaceAll(" ", "").isEmpty()) return; // Usually this is a image/embed, can't send these.
                Bukkit.broadcastMessage(ChatColor.BLUE + "Discord" + divider + event.getAuthor().getName() + ": " + msg);
                break;
        }
    }
}
