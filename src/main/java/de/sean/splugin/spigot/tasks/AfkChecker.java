package de.sean.splugin.spigot.tasks;

/* SPlugin */
import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;

/* Spigot */
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AfkChecker implements Runnable {
    @Override
    public void run() {
        Bukkit.getOnlinePlayers()
            .forEach(this::checkPlayer);
    }

    private void checkPlayer(final Player player) {
        final long lastActivity = SUtil.getLastActivityForPlayer(player.getUniqueId());
        final long curTime = System.currentTimeMillis();

        // 120000 Milliseconds = 2 Minutes
        if ((curTime - lastActivity) > 120000 && !SUtil.isPlayerAFK(player.getUniqueId())) {
            SUtil.setPlayerAFK(player.getUniqueId(), true);
            SMessages.markPlayerAFK(player);
        }
    }
}
