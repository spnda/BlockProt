package de.sean.splugin.spigot.tasks;

/* SPlugin */
import de.sean.splugin.App;
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/* Java */
import java.util.ArrayList;
import java.util.List;

public class SleepChecker implements Runnable {
    public static final List<World> skippingWorlds = new ArrayList<>();

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().stream().map(Entity::getWorld).distinct().filter(this::validateWorld).forEach(this::checkWorld);
    }

    private boolean validateWorld(final World world) {
        // Time between 12950 and 23950 is night time.
        return !skippingWorlds.contains(world) && (world.getTime() > 12950L || world.getTime() < 23950L);
    }

    private void checkWorld(final World world) {
        List<Player> players = world.getPlayers();
        final int amountIgnored = (int) players.stream().filter(SleepChecker::ignorePlayer).count();
        final int amountSleeping = (int) players.stream().filter(LivingEntity::isSleeping).count();
        final int amountNeeded = Math.max(0, (int)Math.ceil((players.size() - amountIgnored) * 0.5 - amountSleeping));
        System.out.println("amountIgnored: " + amountIgnored);
        System.out.println("amountSleeping: " + amountSleeping);
        System.out.println("amountNeeded: " + amountNeeded);
        if (amountNeeded == 0 && amountSleeping > 0) {
            // More than 50% of players are sleeping
            players.forEach(player -> SMessages.sendActionBarMessage(player, SMessages.getRandomMessage("Messages.EveryoneSleeping")));
            skippingWorlds.add(world);
            new SkipNightTask(world).runTaskTimer(App.getInstance(), 0L, 1L);
            players.forEach(player -> player.sendMessage(SMessages.getRandomMessage("Messages.SkipNight")));
        } else if (amountNeeded > 0 && amountSleeping > 0) {
            players.forEach(player -> SMessages.sendActionBarMessage(player, SMessages.getRandomMessage("Messages.Sleeping")));
        }
    }

    private static boolean ignorePlayer(final Player player) {
        // We will ignore AFK players and players who are not in survival
        return player.getGameMode() != GameMode.SURVIVAL || SUtil.isPlayerAFK(player.getUniqueId());
    }
}
