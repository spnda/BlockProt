package de.sean.splugin.discord

import de.sean.splugin.SPlugin
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.bukkit.configuration.file.FileConfiguration
import javax.security.auth.login.LoginException

class DiscordUtil(config: FileConfiguration) {
    private var jda: JDA? = null
    val joinMessage: Boolean
    val leaveMessage: Boolean
    fun sendMessage(message: String?) {
        for ((_, value) in channels) {
            value.sendMessage(message!!).queue()
        }
    }

    companion object {
        val channels: MutableMap<Guild?, TextChannel> = HashMap()
    }

    init {
        val token = config.getString("discord.token")
        joinMessage = config.getBoolean("discord.joinMessage")
        leaveMessage = config.getBoolean("discord.leaveMessage")
        // Only initialize discord stuff if a guild, channel and token are present.
        if (token != null) {
            val builder = JDABuilder.createDefault(token)
            try {
                builder.setActivity(Activity.playing("Minecraft"))
                jda = builder.build()
                jda!!.awaitReady()
                jda!!.addEventListener(SHandler()) // Add event listeners for discord
                SPlugin.instance.logger.info("Discord has started successfully!")
            } catch (e: LoginException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            for ((key, value) in config.getConfigurationSection("discord.channels")!!.getValues(true)) {
                val guild = jda!!.getGuildById(key!!)
                if (guild != null) {
                    val channel = guild.getTextChannelById(value.toString())
                    if (channel != null) channels[guild] = channel
                }
            }
        }
    }
}
