package de.sean.splugin.spigot.tasks

import de.sean.splugin.util.SMessages.getRandomMessage
import org.bukkit.Statistic
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

class SkipNightTask(private val world: World) : BukkitRunnable() {
    override fun run() {
        val time = world.time
        if (time < 450L || time > 1000L) {
            world.time = time
        } else {
            world.players.forEach(Consumer { player: Player -> player.sendMessage(getRandomMessage("messages.nightSkipped")) })
            SleepChecker.skippingWorlds.remove(world)
            world.players.forEach(Consumer { player: Player -> player.setStatistic(Statistic.TIME_SINCE_REST, 0) })
            world.setStorm(false)
            world.isThundering = false
            cancel()
        }
    }
}
