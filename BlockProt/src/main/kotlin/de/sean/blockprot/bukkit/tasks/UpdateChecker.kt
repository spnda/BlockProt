package de.sean.blockprot.bukkit.tasks

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.bukkit.Bukkit
import org.bukkit.plugin.PluginDescriptionFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors

class UpdateChecker(private val description: PluginDescriptionFile) : Runnable {
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
            when {
                Version(latest.currentVersion) > Version(description.version) ->
                    Bukkit.getLogger().warning("${description.name} is outdated. Current: ${description.version} / Newest: ${latest.currentVersion}.")
                Version(latest.currentVersion) < Version(description.version) ->
                    Bukkit.getLogger().info("${description.name} is on Version ${description.version}, even though latest is ${latest.currentVersion}.")
                else ->
                    Bukkit.getLogger().info("${description.name} is up to date. (${latest.currentVersion}).")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    }

    /**
     * A spigot resource object from the SpigotMC API
     */
    data class SpigotResource(
        @SerializedName("id") val id: String?,
        @SerializedName("current_version") val currentVersion: String,
        @SerializedName("tag") val tag: String,
    )

    /**
     * A semantic versioning helper class to compare two versions
     */
    class Version(version: String) {
        private val parts = version.split(".")

        operator fun compareTo(other: Version): Int {
            val length = Math.max(parts.size, other.parts.size)
            for (i in 0..length) {
                val part = if (i < parts.size) parts[i].toInt() else 0
                val otherPart = if (i < other.parts.size) other.parts[i].toInt() else 0
                if (part < otherPart) return -1
                if (part > otherPart) return 1
            }
            return 0
        }

        override operator fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (other !is Version) return false
            return this.compareTo(other) == 0
        }

        override fun hashCode(): Int {
            return parts.hashCode()
        }
    }
}
