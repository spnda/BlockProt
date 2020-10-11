package de.sean.splugin.spigot.tasks

import de.sean.splugin.util.SMessages.markPlayerAFK
import de.sean.splugin.util.SUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class AfkChecker : Runnable {
    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player: Player -> checkPlayer(player) }
    }

    private fun checkPlayer(player: Player) {
        val lastActivity = SUtil.playerLastActivity[player.uniqueId]
        val curTime = System.currentTimeMillis()

        // 120000 Milliseconds = 2 Minutes
        if (curTime - lastActivity!! > 120000 && !SUtil.afkPlayers[player.uniqueId]!!) {
            SUtil.afkPlayers[player.uniqueId] = true
            markPlayerAFK(player)
        }
    }
}
