package de.sean.splugin.paper.tasks

import org.bukkit.entity.Player
import java.util.logging.Logger

class ClientBrandLogger(val player: Player) : Runnable {
    override fun run() {
        Logger.getLogger(this.javaClass.simpleName).info("${player.displayName} has joined with ${player.clientBrandName}")
    }
}
