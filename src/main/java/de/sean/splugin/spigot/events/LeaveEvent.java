package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.discord.DiscordUtil;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Spigot */
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {
    @EventHandler
    public void PlayerLeave(PlayerQuitEvent event) {
        /* Format leave message */
        event.setQuitMessage(ChatColor.RED + SMessages.getRandomMessage("Messages.Leave").replace("[player]", event.getPlayer().getName()));
        
        /* AFK */
        SUtil.removeActivityForPlayer(event.getPlayer().getUniqueId());
        SUtil.removePlayerAFK(event.getPlayer().getUniqueId());

        /* Discord */
        DiscordUtil discord = App.getInstance().getDiscordUtil();
        if (discord.leaveMessage) {
            discord.sendMessage(SMessages.getRandomMessage("Messages.Leave").replace("[player]", event.getPlayer().getName()));
        }
    }
}
