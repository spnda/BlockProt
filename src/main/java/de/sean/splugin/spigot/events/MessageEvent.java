package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.discord.DiscordUtil;
import de.sean.splugin.util.PlayerType;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Spigot */
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MessageEvent implements Listener {
    @EventHandler
    public void PlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerType pt = PlayerType.getPlayerTypeForPlayer(player.getUniqueId());
        String message = event.getMessage();

        /* Chat formatting */
        String ptString = pt.name;
        if (!player.getDisplayName().split("|")[0].equals(ptString)) {
            String playerNickname = pt.color + ptString + " | " + ChatColor.RESET + player.getName();
            player.setDisplayName(playerNickname);
            player.setPlayerListName(playerNickname);
        }
        final String messageFormat = "%1$s: %2$s";
        event.setFormat(messageFormat);
        event.setMessage(message);

        /* AFK: We unmark the player AFK when they write a message. */
        SUtil.setLastActivityForPlayer(player.getUniqueId(), System.currentTimeMillis());
        if (SUtil.isPlayerAFK(player.getUniqueId())) {
            SUtil.setPlayerAFK(player.getUniqueId(), false);
            SMessages.unmarkPlayerAFK(player);
        }

        /* Discord */
        DiscordUtil discord = App.getInstance().getDiscordUtil();
        discord.sendMessage("**" + player.getName() + "**: " + message);
    }
}
