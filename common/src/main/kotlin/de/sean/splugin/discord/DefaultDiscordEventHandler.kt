package de.sean.splugin.discord

import de.sean.splugin.util.ColorUtil
import de.sean.splugin.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.ArrayList
import java.util.logging.Logger

class DefaultDiscordEventHandler : DiscordEventHandler() {
    override fun onReady(event: ReadyEvent) {
        Logger.getLogger("DefaultDiscordEventHandler").info("Discord has started! ${event.jda.selfUser.name}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        val message = event.message.contentRaw.split(" ").toTypedArray()
        if (event.channelType == ChannelType.PRIVATE) {
            if (message[0] == "?msg") {
                if (message.size < 3) {
                    event.channel.sendMessage("Not enough arguments. Usage: `?msg <Player Name> <Message>`").queue()
                } else {
                    val user = event.author
                    val player = Bukkit.getPlayer(message[1])
                    player?.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + user.name + " whispers to you: " + Util.concatArrayRange(message, 2, message.size))
                }
            }
        }
        val eb: EmbedBuilder
        when (message[0]) {
            "?help" -> {
                eb = EmbedBuilder()
                eb.setColor(ColorUtil.randomColor())
                eb.setTitle("Server Help", null)
                eb.setDescription("This bot links a minecraft server with a discord bot. https://github.com/spnda/SPlugin")
                eb.addField("Private Messages", "Using `?msg` you can whisper to any player currently online on the server.", false)
                eb.addField("Online Players", "`?players` will give you a neat list of all online players.", false)
                event.channel.sendMessage(eb.build()).queue()
            }
            "?players" -> {
                eb = EmbedBuilder()
                eb.setColor(ColorUtil.randomColor())
                eb.setTitle("Online Players", null)
                val players: List<Player> = ArrayList(Bukkit.getOnlinePlayers())
                eb.setDescription("There are " + players.size + " / " + Bukkit.getMaxPlayers() + " players online.")
                val playerList = StringBuilder()
                for (player in players) {
                    playerList.append(player.displayName.replace("§[a-z]".toRegex(), "")).append("\n")
                }
                if (playerList.isNotEmpty()) eb.addField("Players online", playerList.toString(), false)
                event.channel.sendMessage(eb.build()).queue()
            }
            else -> {
                val channel = Discord.instance.channels[event.guild] ?: return
                if (channel != event.textChannel) return
                var msg = event.message.contentStripped
                if (event.message.attachments.size > 0) msg += if (msg.isNotEmpty()) " " else "" + "[file]" // Show a nice indicator that the person has sent a image.
                val format = Discord.instance.discordFormat ?: "§9[user]: §r[message]" // If there is no format specified, we'll use a default one similar to minecraft's messages
                Bukkit.broadcastMessage(format.replace("[user]", event.author.name).replace("[message]", msg))
            }
        }
    }
}
