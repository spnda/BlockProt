package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import net.md_5.bungee.api.ChatColor;

public class BedEnterEvent implements Listener {
    @EventHandler
    public void PlayerEnterBed(PlayerBedEnterEvent event) {
        /* Night Skipper */
        if (!event.getBedEnterResult().equals(BedEnterResult.OK)) return;
        App plugin = App.getInstance();
        Player player = event.getPlayer();
        //  Check if this is the overworld...
        Bukkit.getScheduler().runTaskLater(plugin, ()  -> Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " schl\u00E4ft jetzt!"), 1);
    }
}
