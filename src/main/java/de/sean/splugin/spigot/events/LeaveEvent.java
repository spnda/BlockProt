package de.sean.splugin.spigot.events;

import de.sean.splugin.SPlugin;
import de.sean.splugin.discord.DiscordUtil;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {
    @EventHandler
    public void PlayerLeave(PlayerQuitEvent event) {
        /* Format leave message */
        event.setQuitMessage(ChatColor.RED + SMessages.getRandomMessage("messages.leave").replace("[player]", event.getPlayer().getName()));
        
        /* AFK */
        SUtil.removeActivityForPlayer(event.getPlayer().getUniqueId());
        SUtil.removePlayerAFK(event.getPlayer().getUniqueId());

        /* Discord */
        final DiscordUtil discord = SPlugin.discord;
        if (discord.leaveMessage) {
            discord.sendMessage(SMessages.getRandomMessage("messages.leave").replace("[player]", event.getPlayer().getName()));
        }
    }
}
