package de.sean.splugin.spigot.tasks

import de.sean.splugin.SPlugin
import de.sean.splugin.util.SMessages.getRandomMessage
import de.sean.splugin.util.SMessages.sendActionBarMessage
import de.sean.splugin.util.SUtil
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer
import kotlin.math.ceil

class SleepChecker : Runnable {
    override fun run() {
        Bukkit.getOnlinePlayers().stream().map { obj: Entity -> obj.world }.distinct().filter { world: World -> validateWorld(world) }.forEach { world: World -> checkWorld(world) }
    }

    private fun validateWorld(world: World): Boolean {
        // Time between 12950 and 23950 is night time.
        return !skippingWorlds.contains(world) && (world.time > 12950L || world.time < 23950L)
    }

    private fun checkWorld(world: World) {
        val players = world.players
        val amountIgnored = players.stream().filter { player: Player -> ignorePlayer(player) }.count().toInt()
        val amountSleeping = players.stream().filter { obj: Player -> obj.isSleeping }.count().toInt()
        val amountNeeded = Math.max(0, ceil(Math.max(0, players.size - amountIgnored) * (SPlugin.instance.config.getInt("skipNight.percentage") / 100.0) - amountSleeping).toInt())
        if (amountNeeded == 0 && amountSleeping > 0) {
            // More than 50% of players are sleeping
            players.forEach(Consumer { player: Player? -> sendActionBarMessage(player!!, getRandomMessage("messages.everyoneSleeping")) })
            skippingWorlds.add(world)
            SkipNightTask(world).runTaskTimer(SPlugin.instance, 0L, 1L)
            players.forEach(Consumer { player: Player -> player.sendMessage(getRandomMessage("messages.skipNight")) })
        } else if (amountNeeded > 0 && amountSleeping > 0) {
            val message = getRandomMessage("messages.sleeping").replace("[sleeping]", amountSleeping.toString()).replace("[needed]", amountNeeded.toString())
            players.forEach(Consumer { player: Player? -> sendActionBarMessage(player!!, message) })
        }
    }

    companion object {
        val skippingWorlds: MutableList<World> = ArrayList()
        private fun ignorePlayer(player: Player): Boolean {
            // We will ignore AFK players and players who are not in survival
            return player.gameMode != GameMode.SURVIVAL || SUtil.afkPlayers[player.uniqueId]!!
        }
    }
}
