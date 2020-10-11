package de.sean.splugin.spigot.events;

import de.sean.splugin.SPlugin;
import de.sean.splugin.discord.DiscordUtil;
import de.sean.splugin.util.PlayerType;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {
    @EventHandler
    public void PlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        /* Format Join Message */
        if (!player.hasPlayedBefore()) {
            // The player just joined for the first time, introduce the player to the server.
            SMessages.sendTitleMessage(player, SMessages.getRandomMessage("messages.welcome").replace("[player]", player.getDisplayName()), "");
        } else {
            // The player is not playing for the first time, just welcome the player.
            SMessages.sendTitleMessage(player, SMessages.getRandomMessage("messages.welcomeBack").replace("[player]", player.getDisplayName()), "");
        }
        event.setJoinMessage(ChatColor.GREEN + SMessages.getRandomMessage("messages.join").replace("[player]", event.getPlayer().getDisplayName()));

        /* AFK */
        SUtil.setLastActivityForPlayer(player.getUniqueId(), System.currentTimeMillis());
        SUtil.setPlayerAFK(player.getUniqueId(), false);

        /* Format Player Display Name */
        final String role = SPlugin.instance.getConfig().getString("players." + player.getUniqueId() + ".role");
        final PlayerType pt = PlayerType.setPlayerTypeForPlayer(player.getUniqueId(), PlayerType.getForId(role));
        // Here, we will check if the current display name matches the username.
        // If it doesn't, the player is inside a spigot permission group which has a prefix defined.
        // Therefore, we don't want to add any other prefix to the name.
        if (pt != null && SPlugin.instance.getConfig().getBoolean("feature.showGroup")) {
            String playerNickname, ptString = pt.name;
            playerNickname = pt.color + ptString + " | " + ChatColor.RESET + player.getName();
            player.setDisplayName(playerNickname);
            player.setPlayerListName(playerNickname);
        }

        /* Discord */
        final DiscordUtil discord = SPlugin.discord;
        if (discord.joinMessage) {
            discord.sendMessage(SMessages.getRandomMessage("messages.join").replace("[player]", player.getName()));
        }
    }
}
