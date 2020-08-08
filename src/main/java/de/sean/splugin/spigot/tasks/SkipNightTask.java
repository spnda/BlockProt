package de.sean.splugin.spigot.tasks;

/* Spigot */
import de.sean.splugin.util.SMessages;
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
        if (time < 450 || time > 1000) {
            this.world.setTime(time);
        } else {
            this.world.getPlayers().forEach(player -> player.sendMessage(SMessages.getRandomMessage("Messages.NightSkipped")));
            SleepChecker.skippingWorlds.remove(this.world);
            this.world.setStorm(false);
            this.world.setThundering(false);
            this.cancel();
        }
    }
}
