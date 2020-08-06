package de.sean.splugin.spigot.events;

/* SPlugin */
import de.sean.splugin.App;

/* Spigot */
import de.sean.splugin.util.SMessages;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;

public class BedEnterEvent implements Listener {
    @EventHandler
    public void PlayerEnterBed(PlayerBedEnterEvent event) {
        /* Night Skipper */
        if (!event.getBedEnterResult().equals(BedEnterResult.OK)) return;
        App plugin = App.getInstance();
        Player player = event.getPlayer();
        Bukkit.broadcastMessage(SMessages.getRandomMessage("Messages.SleepEnter").replace("[player]", event.getPlayer().getDisplayName()));
    }
}
