/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.sean.blockprot.bukkit.tasks

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.sean.blockprot.BlockProt
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
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors

class UpdateChecker(private val receivingPlayers: List<Player>, private val description: PluginDescriptionFile) : Runnable {
    override fun run() {
        var inputStream: InputStream? = null
        try {
            // Documentation for API at https://github.com/SpigotMC/XenforoResourceManagerAPI
            val url = URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=87829")
            val request = url.openConnection()
            request.connect()
            inputStream = request.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream!!))
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
            BlockProt.getInstance().logger.warning(e.toString())
            return
        } finally {
            inputStream?.close()
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
