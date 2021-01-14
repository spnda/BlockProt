package de.sean.splugin.bukkit.tasks

import de.sean.splugin.util.Messages.getRandomMessage
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

object AfkPlayerManager {
    private var initialized: Boolean = false
    private var markAfkMessages: MutableList<*>? = null
    private var unmarkAfkMessages: MutableList<*>? = null

    private val playerLastActivity = HashMap<UUID, Long>()
    private val afkPlayers = HashMap<UUID, Boolean>()

    fun init(config: FileConfiguration) {
        initialized = true
        markAfkMessages = config.getList("messages.afk", ArrayList<Any>())
        unmarkAfkMessages = config.getList("messages.not_afk", ArrayList<Any>())
    }

    fun getLastActivity(player: UUID): Long = playerLastActivity[player] ?: System.currentTimeMillis()

    fun setLastActivity(player: UUID, time: Long) {
        playerLastActivity[player] = time
    }

    fun isAfk(player: UUID): Boolean = afkPlayers[player] ?: false

    fun setAfk(player: UUID, value: Boolean) {
        afkPlayers[player] = value
    }

    fun markPlayerAfk(player: Player) {
        Bukkit.broadcastMessage(getRandomMessage(markAfkMessages).replace("[player]", player.displayName))
        player.setDisplayName(player.displayName + " (AFK)")
        player.setPlayerListName(player.playerListName + " (AFK)")
    }

    fun unmarkPlayerAfk(player: Player) {
        player.setPlayerListName(player.playerListName.replace(" (AFK)", ""))
        player.setDisplayName(player.displayName.replace(" (AFK)", ""))
        Bukkit.broadcastMessage(getRandomMessage(unmarkAfkMessages).replace("[player]", player.displayName))
    }

    fun remove(player: Player) {
        playerLastActivity.remove(player.uniqueId)
        afkPlayers.remove(player.uniqueId)
    }
}
