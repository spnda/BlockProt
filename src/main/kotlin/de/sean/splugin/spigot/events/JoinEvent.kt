package de.sean.splugin.spigot.events

import de.sean.splugin.SPlugin
import de.sean.splugin.util.PlayerType
import de.sean.splugin.util.SMessages.getRandomMessage
import de.sean.splugin.util.SMessages.sendTitleMessage
import de.sean.splugin.util.SUtil
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinEvent : Listener {
    @EventHandler
    fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player

        /* Format Join Message */if (!player.hasPlayedBefore()) {
            // The player just joined for the first time, introduce the player to the server.
            sendTitleMessage(player, getRandomMessage("messages.welcome").replace("[player]", player.displayName), "")
        } else {
            // The player is not playing for the first time, just welcome the player.
            sendTitleMessage(player, getRandomMessage("messages.welcomeBack").replace("[player]", player.displayName), "")
        }
        event.joinMessage = getRandomMessage("messages.join").replace("[player]", event.player.displayName)

        /* AFK */
        SUtil.playerLastActivity[player.uniqueId] = System.currentTimeMillis()
        SUtil.afkPlayers[player.uniqueId] = false

        /* Format Player Display Name */
        val role = SPlugin.instance.config.getString("players." + player.uniqueId + ".role")
        if (role != null) {
            val pt = PlayerType.setPlayerTypeForPlayer(player.uniqueId, PlayerType.getForId(role))
            // Here, we will check if the current display name matches the username.
            // If it doesn't, the player is inside a spigot permission group which has a prefix defined.
            // Therefore, we don't want to add any other prefix to the name.
            if (SPlugin.instance.config.getBoolean("feature.showGroup")) {
                val playerNickname: String
                val ptString = pt.name
                playerNickname = pt.color.toString() + ptString + " | " + ChatColor.RESET + player.name
                player.setDisplayName(playerNickname)
                player.setPlayerListName(playerNickname)
            }
        }

        /* Discord */
        val discord = SPlugin.discord
        if (discord.joinMessage) {
            discord.sendMessage(getRandomMessage("messages.join").replace("[player]", player.name))
        }
    }
}
