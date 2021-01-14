package de.sean.splugin.bukkit.tasks

import org.bukkit.Bukkit
import org.bukkit.entity.Player

class AfkChecker : Runnable {
    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player: Player -> checkPlayer(player) }
    }

    private fun checkPlayer(player: Player) {
        val lastActivity = AfkPlayerManager.getLastActivity(player.uniqueId)
        val curTime = System.currentTimeMillis()

        // 120000 Milliseconds = 2 Minutes
        if (curTime - lastActivity > 120000 && !AfkPlayerManager.isAfk(player.uniqueId)) {
            AfkPlayerManager.setAfk(player.uniqueId, true)
            AfkPlayerManager.markPlayerAfk(player)
        }
    }
}
