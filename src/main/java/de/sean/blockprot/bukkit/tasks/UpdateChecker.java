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
package de.sean.blockprot.bukkit.tasks;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import de.sean.blockprot.util.SemanticVersion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public final class UpdateChecker implements Runnable {
    @Nullable
    private static SemanticVersion latestVersion;

    @Nullable
    private final List<Player> recipients;

    @NotNull
    private final PluginDescriptionFile description;

    /**
     * Creates a new update checker. This uses a empty list of players and
     * therefore only prints the message to the console.
     *
     * @param description The plugin.yml file of the plugin. See
     *                    {@link JavaPlugin#getDescription()}.
     */
    public UpdateChecker(@NotNull final PluginDescriptionFile description) {
        this.description = description;
        this.recipients = null;
    }

    /**
     * Creates a new update checker. This exclusively messages the players
     * that were passed in the list.
     *
     * @param description The plugin.yml file of the plugin. See
     *                    {@link JavaPlugin#getDescription()}.+
     * @param recipients  The list of players to message.
     */
    public UpdateChecker(@NotNull final PluginDescriptionFile description, @Nullable final List<Player> recipients) {
        this.recipients = recipients;
        this.description = description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        if (latestVersion != null) {
            // Use the cached result.
            this.sendMessage(new SemanticVersion(description.getVersion()), latestVersion);
        } else {
            // Fetch the newest version.
            try {
                // Documentation for API at https://github.com/SpigotMC/XenforoResourceManagerAPI
                URL url = new URL("https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=87829");
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(inputStream));

                SpigotResource latest = new Gson().fromJson(reader, SpigotResource.class);
                SemanticVersion latestVersion = latest.asSemantic();
                UpdateChecker.latestVersion = latestVersion;
                SemanticVersion ourVersion = new SemanticVersion(description.getVersion());

                this.sendMessage(ourVersion, latestVersion);

                inputStream.close();
            } catch (IOException ignored) {

            }
        }
    }

    private void sendMessage(SemanticVersion currentVersion, SemanticVersion latestVersion) {
        String message;
        boolean isOutdated = false;
        if (latestVersion.compareTo(currentVersion) > 0) {
            isOutdated = true;
            message = description.getName() + " is outdated. Current: " + currentVersion + " / Newest: " + latestVersion;
        } else if (latestVersion.compareTo(currentVersion) < 0) {
            message = description.getName() + " is on Version " + currentVersion + ", even though latest is " + latestVersion;
        } else {
            message = description.getName() + " is up to date. (" + currentVersion + ")";
        }

        if (this.recipients != null && !this.recipients.isEmpty()) {
            TextComponent component = new TextComponent(message);
            if (isOutdated) {
                component.setColor(ChatColor.YELLOW);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/blockprot.87829/"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Visit the website to update!")));
            }
            for (Player player : recipients) {
                player.spigot().sendMessage(component);
            }
        } else {
            if (isOutdated) {
                Bukkit.getLogger().warning(message);
            } else {
                Bukkit.getLogger().info(message);
            }
        }
    }

    /**
     * Represents a spigot resource from the Spigot API.
     * See https://github.com/SpigotMC/XenforoResourceManagerAPI#getresource
     * for the exact documentation on this class.
     */
    public static class SpigotResource {
        @SerializedName("current_version")
        public String currentVersion;

        /**
         * Converts the {@link #currentVersion} to a {@link SemanticVersion},
         * for easily comparing the version.
         *
         * @return The semantic version of this current version.
         */
        public SemanticVersion asSemantic() {
            return new SemanticVersion(this.currentVersion);
        }
    }
}
