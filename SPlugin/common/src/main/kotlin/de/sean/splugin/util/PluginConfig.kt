package de.sean.splugin.util

import org.bukkit.configuration.file.FileConfiguration
import java.util.ArrayList

class PluginConfig(config: FileConfiguration) {
    companion object {
        lateinit var instance: PluginConfig
    }

    val configuration: FileConfiguration

    init {
        instance = this
        configuration = config
    }

    /**
     * Returns a random message from a list at given `messageList` inside the config
     */
    fun getRandomMessage(messageList: String): String {
        val messages = configuration.getList(messageList, ArrayList<Any>())
        if (messages != null && messages.isNotEmpty()) {
            if (messages.size == 1) return messages[0] as String
            val index = MathUtil.randomInt(0, messages.size)
            return messages[index] as String
        }
        // If no messages we're defined, return a empty string
        return ""
    }

    fun getRandomMessageOrDefault(messageList: String, default: String): String {
        val message = getRandomMessage(messageList)
        return if (message.isEmpty()) default else message
    }

    fun getMessageCount(messageList: String): Int {
        return (configuration.getList(messageList, ArrayList<Any>()) ?: ArrayList<Any>()).size
    }
}
