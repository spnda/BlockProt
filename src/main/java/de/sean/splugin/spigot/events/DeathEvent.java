package de.sean.splugin.spigot.events;

import de.sean.splugin.SPlugin;
import de.sean.splugin.util.SMessages;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {
    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event) {
        // Some more formatting in the future?
        final String message = SMessages.getRandomMessage("messages.death").replace("[message]", event.getDeathMessage()).replace("[player]", event.getEntity().getDisplayName());
        event.setDeathMessage(ChatColor.RED + message);
        
        /* Discord */
        SPlugin.discord.sendMessage(message);
    }
}
