package de.sean.splugin.util

import de.sean.splugin.SPlugin
import de.sean.splugin.util.SUtil.randomInt
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object SMessages {
    fun markPlayerAFK(player: Player) {
        Bukkit.broadcastMessage(getRandomMessage("messages.afk").replace("[player]", player.displayName))
        player.setDisplayName(player.displayName + " (AFK)")
        player.setPlayerListName(player.playerListName + " (AFK)")
    }

    fun unmarkPlayerAFK(player: Player) {
        player.setPlayerListName(player.playerListName.replace(" (AFK)", ""))
        player.setDisplayName(player.displayName.replace(" (AFK)", ""))
        Bukkit.broadcastMessage(getRandomMessage("messages.not_afk").replace("[player]", player.displayName))
    }

    fun getRandomMessage(messageList: String?): String {
        val messages = SPlugin.instance.config.getList(messageList!!, ArrayList<Any>())
        if (messages != null && messages.isNotEmpty()) {
            if (messages.size == 1) return messages[0] as String
            val index = randomInt(0, messages.size)
            return messages[index] as String
        }
        // If no messages we're defined, return a empty string
        return ""
    }

    fun sendActionBarMessage(player: Player, message: String?) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(message))
    }

    fun sendTitleMessage(player: Player, title: String?, subtitle: String?) {
        player.sendTitle(title, subtitle, 10, 80, 20)
    }
}
