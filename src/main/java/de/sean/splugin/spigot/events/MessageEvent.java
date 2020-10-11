package de.sean.splugin.spigot.events;

import de.sean.splugin.SPlugin;
import de.sean.splugin.util.PlayerType;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MessageEvent implements Listener {
    @EventHandler
    public void PlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final PlayerType pt = PlayerType.getPlayerTypeForPlayer(player.getUniqueId());
        final String message = event.getMessage();

        /* Chat formatting */
        final String ptString = pt.name;
        if (!player.getDisplayName().split("|")[0].equals(ptString)) {
            if (!ptString.equals("")) {
                final String playerNickname = pt.color + ptString + " | " + ChatColor.RESET + player.getName();
                player.setDisplayName(playerNickname);
                player.setPlayerListName(playerNickname);
            }
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
        SPlugin.discord.sendMessage("**" + player.getName() + "**: " + message);
    }
}
