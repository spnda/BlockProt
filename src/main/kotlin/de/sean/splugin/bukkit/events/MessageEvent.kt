package de.sean.splugin.bukkit.events

import de.sean.splugin.SPlugin
import de.sean.splugin.util.PlayerType
import de.sean.splugin.util.SMessages.unmarkPlayerAFK
import de.sean.splugin.util.SUtil
import net.md_5.bungee.api.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class MessageEvent : Listener {
    @EventHandler
    fun playerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val message = event.message

        /* Chat formatting */
        val pt = PlayerType.getPlayerTypeForPlayer(player.uniqueId)
        if (pt != null) {
            val ptString = pt.name
            if (player.displayName.split("|").toTypedArray()[0] != ptString) {
                if (ptString != "") {
                    val playerNickname = pt.color.toString() + ptString + " | " + ChatColor.RESET + player.name
                    player.setDisplayName(playerNickname)
                    player.setPlayerListName(playerNickname)
                }
            }
        }

        /* Chat Message formatting */
        val config = SPlugin.instance.config
        if (config.getBoolean("features.chatFormat")) {
            if (config.getString("chatFormat.format") != null) {
                val format = config.getString("chatFormat.format") ?: "[player]: [message]"
                event.format = format.replace("[player]", "%1\$s").replace("[message]", "%2\$s")
            }
        }

        /* AFK: We unmark the player AFK when they write a message. */
        SUtil.playerLastActivity[player.uniqueId] = System.currentTimeMillis()
        if (SUtil.afkPlayers[player.uniqueId]!!) {
            SUtil.afkPlayers[player.uniqueId] = false
            unmarkPlayerAFK(player)
        }

        /* Discord */
        SPlugin.discord.sendMessage("**" + player.name + "**: " + message)
    }
}
