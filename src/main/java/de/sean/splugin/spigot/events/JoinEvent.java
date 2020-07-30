package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import de.sean.splugin.util.SUtil.PlayerType;

/* Spigot */
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/* Discord */
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class JoinEvent implements Listener {
    @EventHandler
    public void PlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        /* Format Join Message */
        if (!player.hasPlayedBefore()) {
            // The player just joined for the first time, introduce the player to the server.
            SMessages.sendTitleMessage(player, "Willkommen auf dem 9D Server!", "");
        } else {
            // The player is not playing for the first time, just welcome the player.
            SMessages.sendTitleMessage(player, "Willkommen zur\u00FCck!", "");
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
    }
}
