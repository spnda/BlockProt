package de.sean.blockprot.bukkit.events

import de.sean.blockprot.BlockProt
import de.sean.blockprot.bukkit.tasks.UpdateChecker
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinEvent : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val config = BlockProt.instance.config
        val configSetting = config.get("notify_op_of_updates")
        if (configSetting is Boolean && configSetting && event.player.isOp) {
            Bukkit.getScheduler().runTaskAsynchronously(BlockProt.instance, UpdateChecker(listOf(event.player), BlockProt.instance.description))
        }
    }
}
