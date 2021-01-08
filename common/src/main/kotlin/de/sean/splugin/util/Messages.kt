package de.sean.splugin.util

import de.sean.splugin.util.MathUtil.randomInt
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

object Messages {
    fun getRandomMessage(messages: MutableList<*>?): String {
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
