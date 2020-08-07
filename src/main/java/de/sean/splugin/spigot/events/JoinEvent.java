package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import de.sean.splugin.util.SUtil.PlayerType;

/* Spigot */
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/* Discord */
import net.dv8tion.jda.api.JDA;

public class JoinEvent implements Listener {
    @EventHandler
    public void PlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        /* Format Join Message */
        if (!player.hasPlayedBefore()) {
            // The player just joined for the first time, introduce the player to the server.
            SMessages.sendTitleMessage(player, SMessages.getRandomMessage("Messages.Welcome").replace("[player]", player.getDisplayName()), "");
        } else {
            // The player is not playing for the first time, just welcome the player.
            SMessages.sendTitleMessage(player, SMessages.getRandomMessage("Messages.WelcomeBack").replace("[player]", player.getDisplayName()), "");
        }
        event.setJoinMessage(ChatColor.GREEN + SMessages.getRandomMessage("Messages.Join").replace("[player]", event.getPlayer().getDisplayName()));

        /* AFK */
        SUtil.setLastActivityForPlayer(player.getUniqueId(), System.currentTimeMillis());
        SUtil.setPlayerAFK(player.getUniqueId(), false);

        /* Format Player Display Name */
        PlayerType pt = SUtil.getPlayerType(player);
        String playerNickname, ptString = SUtil.getStringForPlayerType(pt);
        playerNickname = SUtil.getChatColorForPlayerType(pt) + ptString + " | " + ChatColor.RESET + player.getName();
        player.setDisplayName(playerNickname);
        player.setPlayerListName(playerNickname);

        /* Discord */
        if (App.getInstance().getConfig().getBoolean("Discord.JoinMessage")) {
            JDA jda = App.getInstance().getDiscordInstance();
            Guild guild = jda.getGuildById(SUtil.GUILD_ID);
            if (guild == null) return;
            TextChannel channel = guild.getTextChannelById(SUtil.CHANNEL_ID);
            if (channel != null) channel.sendMessage(SMessages.getRandomMessage("Messages.Join").replace("[player]", player.getDisplayName())).queue();
        }
    }
}
