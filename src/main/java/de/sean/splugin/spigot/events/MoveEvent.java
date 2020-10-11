package de.sean.splugin.spigot.events;

import de.sean.splugin.util.SMessages;
import de.sean.splugin.util.SUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveEvent implements Listener {
    @EventHandler
    public void PlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        SUtil.setLastActivityForPlayer(player.getUniqueId(), System.currentTimeMillis());
        if (SUtil.isPlayerAFK(player.getUniqueId())) {
            SUtil.setPlayerAFK(player.getUniqueId(), false);
            SMessages.unmarkPlayerAFK(player);
        }
    }
}