package de.sean.blockprot.bukkit.tasks

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.sean.blockprot.util.BlockProtMessenger
import de.sean.blockprot.util.SemanticVersion
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginDescriptionFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors

class UpdateChecker(private val receivingPlayers: List<Player>, private val description: PluginDescriptionFile) : Runnable {
    override fun run() {
        try {
            // Documentation for API at https://github.com/SpigotMC/XenforoResourceManagerAPI
            val url = URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=87829")
            val request = url.openConnection()
            request.connect()
            val inputStream = request.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response: String = reader.lines().collect(Collectors.joining(System.lineSeparator()))
            // Use Gson to parse the given JSON from the API to a SpigotResource class.
            val latest: SpigotResource = Gson().fromJson(response, SpigotResource::class.java)
            val latestVersion = latest.asSemanticVersion()
            when {
                latestVersion > SemanticVersion(description.version) ->
                    log("${description.name} is outdated. Current: ${description.version} / Newest: ${latest.currentVersion}.", true)
                latestVersion < SemanticVersion(description.version) ->
                    log("${description.name} is on Version ${description.version}, even though latest is ${latest.currentVersion}.", true)
                else ->
                    log("${description.name} is up to date. (${latest.currentVersion}).", false, BlockProtMessenger.LogSeverity.LOG)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }

    private fun log(content: String, isOutdated: Boolean, severity: BlockProtMessenger.LogSeverity = BlockProtMessenger.LogSeverity.WARN) {
        if (receivingPlayers.isNotEmpty()) {
            val message = TextComponent(content)
            if (isOutdated) {
                message.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/blockprot.87829/")
                message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Visit the website to update!"))
            }
            for (player in receivingPlayers) {
                player.spigot().sendMessage(message)
            }
        } else {
            BlockProtMessenger.log(content, severity)
        }
    }

    /**
     * A spigot resource object from the SpigotMC API
     */
    data class SpigotResource(
        @SerializedName("id") val id: String?,
        @SerializedName("current_version") val currentVersion: String,
        @SerializedName("tag") val tag: String,
    ) {
        fun asSemanticVersion(): SemanticVersion {
            return SemanticVersion(currentVersion)
        }
    }
}
