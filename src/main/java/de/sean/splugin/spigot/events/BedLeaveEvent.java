package de.sean.splugin.spigot.events;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import net.md_5.bungee.api.ChatColor;

public class BedLeaveEvent implements Listener {
    @EventHandler
    public void PlayerLeaveBed(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        //  Check if this is the overworld...
        Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " ist aufgestanden!");
    }
}
