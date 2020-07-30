package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import de.sean.splugin.util.SUtil.PlayerType;

/* Spigot */
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/* Discord */
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;

public class MessageEvent implements Listener {
    // private String citizenFormat        = ChatColor.GREEN   + "B\u00FCrger | "           + ChatColor.RESET + "%1$s: %2$s";
    private String messageFormat = "%1$s: %2$s";

    @EventHandler
    public void PlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerType pt = SUtil.getPlayerType(player);
        String message = event.getMessage();

        /* Chat formatting */
        String ptString = SUtil.getStringForPlayerType(pt);
        if (!player.getDisplayName().split("|")[0].equals(ptString)) {
            String playerNickname = SUtil.getChatColorForPlayerType(pt) + ptString + " | " + ChatColor.RESET + player.getName();
            player.setDisplayName(playerNickname);
            player.setPlayerListName(playerNickname);
        }
        event.setFormat(messageFormat);
        event.setMessage(message);

        /* AFK: We unmark the player AFK when they write a message. */
        SUtil.setLastActivityForPlayer(player.getUniqueId(), System.currentTimeMillis());
        if (SUtil.isPlayerAFK(player.getUniqueId())) {
            SUtil.setPlayerAFK(player.getUniqueId(), false);
            SMessages.unmarkPlayerAFK(player);
        }

        /* Discord */
        JDA jda = App.getInstance().getDiscordInstance();
        Guild guild = jda.getGuildById(SUtil.GUILD_ID);
        if (guild == null) return;
        TextChannel channel = guild.getTextChannelById(SUtil.CHANNEL_ID);
        try {
            if (channel != null) channel.sendMessage("**" + player.getName() + "**: " + message).queue();
        } catch (Exception e) {
            App.getInstance().getLogger().severe("Couldn't send message to Discord: " + e.toString());
        }
    }
}
