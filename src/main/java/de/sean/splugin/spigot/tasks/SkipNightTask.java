package de.sean.splugin.spigot.tasks;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;

public class SkipNightTask extends BukkitRunnable {
    private final World world;

    public SkipNightTask(final World world) {
        this.world = world;
    }

    @Override
    public void run() {
        final long time = world.getTime();
        if (time >= 450  && time <= 1000) {
            // Announce night skip
            Bukkit.broadcastMessage(ChatColor.YELLOW + "");
            SleepChecker.skippingWorlds.remove(world);

            // Reset sleep statistic
            world.getPlayers().forEach(player -> player.setStatistic(Statistic.TIME_SINCE_REST, 0));

            this.cancel();
        } else {
            world.setTime(time + 1);
        }
    }
}