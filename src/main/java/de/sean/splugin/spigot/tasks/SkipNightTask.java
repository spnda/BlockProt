package de.sean.splugin.spigot.tasks;

import de.sean.splugin.util.SMessages;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class SkipNightTask extends BukkitRunnable {
    private final World world;

    public SkipNightTask(final World world) {
        this.world = world;
    }

    @Override
    public void run() {
        final long time = this.world.getTime();
        if (time < 450L || time > 1000L) {
            this.world.setTime(time);
        } else {
            this.world.getPlayers().forEach(player -> player.sendMessage(SMessages.getRandomMessage("messages.nightSkipped")));
            SleepChecker.skippingWorlds.remove(this.world);
            this.world.getPlayers().forEach(player -> player.setStatistic(Statistic.TIME_SINCE_REST, 0));
            this.world.setStorm(false);
            this.world.setThundering(false);
            this.cancel();
        }
    }
}
