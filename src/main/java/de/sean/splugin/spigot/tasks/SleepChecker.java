package de.sean.splugin.spigot.tasks;

/* SPlugin */
import de.sean.splugin.App;

/* Java */
import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SleepChecker implements Runnable {
    public static final List<World> skippingWorlds = new ArrayList<>();

    @Override
    public void run() {
        Bukkit.getOnlinePlayers()
            .stream()
            .map(Player::getWorld).distinct()
            .filter(this::validateWorld)
            .forEach(this::checkWorld);
    }

    private void checkWorld(final World world) {
        final int sleeping = getSleeping(world).size();
        final int needed = getNeeded(world);

        if (sleeping > 0 && needed > 0) {
            // Not sleeping yet.
            Bukkit.broadcastMessage("");
        } else if (needed == 0 && sleeping > 0) {
            // Enough to skip.
            Bukkit.broadcastMessage("");
            skippingWorlds.add(world);

            new SkipNightTask(world).runTaskTimer(App.getInstance(), 0L, 1);
            //Messages.sendRandomChatMessage(world, "messages.chat.accelerateNight");
        }
    }

    private boolean validateWorld(final World world) {
        // TODO: Only check for players in the overworld.
        return !skippingWorlds.contains(world) && !isNight(world);
    }

    private boolean isNight(final World world) {
        return world.getTime() > 12950 || world.getTime() < 23950;
    }

    public static List<Player> getSleeping(final World world) {
        return world.getPlayers().stream().filter(Player::isSleeping).collect(toList());
    }

    public static int getPlayerCount(final World world) {
        return Math.max(0, world.getPlayers().size() - getExcluded(world).size());
    }

    public static int getNeeded(final World world) {
        return Math.max(0, (int)Math.ceil((getPlayerCount(world)) * 0.5 - getSleeping(world).size()));
    }

    private static List<Player> getExcluded(final World world) {
        return world.getPlayers().stream().filter(SleepChecker::isExcluded).collect(toList());
    }

    private static boolean isExcluded(final Player p) {
        return p.getGameMode() != GameMode.SURVIVAL;
    }
}
