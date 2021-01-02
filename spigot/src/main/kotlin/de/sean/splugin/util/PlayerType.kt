package de.sean.splugin.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.FileConfiguration
import java.util.*
import kotlin.collections.HashMap

class PlayerType private constructor(val id: String, val name: String, val color: ChatColor) {
    companion object {
        private val playerTypes: MutableList<PlayerType> = ArrayList()
        private val players = HashMap<UUID, PlayerType>()
        private val DEFAULT = registerPlayerType("DEFAULT", "", ChatColor.WHITE)!!

        fun loadFromConfig(config: FileConfiguration) {
            try {
                // We load the roles at startup
                // The PlayerType of a player gets loaded up when they join the server.
                val roles = config.getConfigurationSection("roles")!!.getValues(false)
                for ((id, value) in roles) {
                    var name: String? = null
                    var color: ChatColor? = null
                    if (value is MemorySection) {
                        val role = value.getValues(false)
                        for ((key, value1) in role) {
                            when (key.toLowerCase()) {
                                "name" -> name = value1.toString()
                                "color" -> color = ChatColor::class.java.getField(value1.toString().toUpperCase())[null] as ChatColor
                            }
                        }
                        if (id != null && name != null) registerPlayerType(id, name, color ?: ChatColor.WHITE)
                    }
                }
            } catch (e: Exception) {
                println(e.toString())
            }
        }

        private fun registerPlayerType(id: String, name: String, color: ChatColor): PlayerType? {
            val playerType = PlayerType(id, name, color)
            if (!playerTypes.contains(playerType)) {
                playerTypes.add(playerType)
                return playerType
            } else {
                println("WARN: Duplicate player role: $id")
            }
            return null
        }

        fun setPlayerTypeForPlayer(uuid: UUID, playerType: PlayerType): PlayerType {
            players[uuid] = playerType
            return playerType
        }

        fun getPlayerTypeForPlayer(uuid: UUID): PlayerType? {
            return players[uuid]
        }

        fun getForId(id: String): PlayerType {
            for (playerType in playerTypes) {
                if (playerType.id == id) return playerType
            }
            return DEFAULT
        }
    }
}
