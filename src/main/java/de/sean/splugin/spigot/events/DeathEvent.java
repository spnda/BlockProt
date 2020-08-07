package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.discord.DiscordUtil;
import de.sean.splugin.util.SMessages;

/* Spigot */
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {
    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event) {
        // Some more formatting in the future?
        String message = SMessages.getRandomMessage("Messages.Death").replace("[message]", event.getDeathMessage()).replace("[player]", event.getEntity().getDisplayName());
        event.setDeathMessage(ChatColor.RED + message);
        
        /* Discord */
        DiscordUtil discord = App.getInstance().getDiscordUtil();
        discord.sendMessage(message);
    }
}
