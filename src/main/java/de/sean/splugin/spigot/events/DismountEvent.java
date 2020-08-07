package de.sean.splugin.spigot.events;

/* Spigot */
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public class DismountEvent implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void entityDismount(final EntityDismountEvent event) {
        // This indicates a player just stopped sitting.
        if (event.getEntity() instanceof Player && event.getDismounted() instanceof Arrow) {
            event.getDismounted().remove();
            event.getEntity().setVelocity(new Vector(.0f, .5f, .0f));
        }
    }
}
