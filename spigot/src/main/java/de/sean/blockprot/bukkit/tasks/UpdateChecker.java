/*
 * Copyright (C) 2021 - 2025 spnda
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
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.util.SemanticVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
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
    
    @NotNull
    private final SemanticVersion currentVersion; // Cache current version at creation time

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
        this.currentVersion = new SemanticVersion(description.getVersion()); // Cache at creation
    }

    /**
     * Creates a new update checker. This exclusively messages the players
     * that were passed in the list.
     *
     * @param description The plugin.yml file of the plugin. See
     *                    {@link JavaPlugin#getDescription()}.
     * @param recipients  The list of players to message.
     */
    public UpdateChecker(@NotNull final PluginDescriptionFile description, @Nullable final List<Player> recipients) {
        this.recipients = recipients;
        this.description = description;
        this.currentVersion = new SemanticVersion(description.getVersion()); // Cache at creation
    }

    @Override
    public void run() {
        if (latestVersion != null) {
            // Use the cached result.
            this.sendMessage(currentVersion, latestVersion);
        } else {
            // Fetch the newest version from Spigot API.
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

                this.sendMessage(currentVersion, latestVersion);

                inputStream.close();
            } catch (IOException ignored) {
                // Network error — silently ignore.
            }
        }
    }

    private void sendMessage(SemanticVersion currentVersion, SemanticVersion latestVersion) {
        String message;
        boolean isOutdated = false;
        if (latestVersion.compareTo(currentVersion) > 0) {
            isOutdated = true;
            message = description.getName() + " v" + currentVersion + " detected, but v" + latestVersion + " is available.";
        } else if (latestVersion.compareTo(currentVersion) < 0) {
            message = description.getName() + " is running v" + currentVersion + " (newer than the latest public release v" + latestVersion + ")";
        } else {
            message = description.getName() + " is up to date (v" + currentVersion + ")";
        }

        if (this.recipients != null && !this.recipients.isEmpty()) {
            var comp = Component.text(message);
            if (isOutdated) {
                comp = comp.color(NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/blockprot.87829/"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to visit the SpigotMC resource page")));
            }
            for (Player player : recipients) {
                player.sendMessage(comp);
            }
        } else {
            if (isOutdated) {
                BlockProt.getInstance().getLogger().warning(message);
            } else {
                BlockProt.getInstance().getLogger().info(message);
            }
        }
    }

    /**
     * Represents a spigot resource from the Spigot API.
     * See <a href="https://github.com/SpigotMC/XenforoResourceManagerAPI#getresource">https://github.com/SpigotMC/XenforoResourceManagerAPI#getresource</a>
     * for the exact documentation on this class.
     */
    public final static class SpigotResource {
        @SerializedName("current_version") String currentVersion;

        /**
         * Converts the {@link #currentVersion} to a {@link SemanticVersion},
         * for easily comparing the version.
         *
         * @return The semantic version of this current version.
         */
        @Contract(" -> new")
        public @NotNull SemanticVersion asSemantic() {
            return new SemanticVersion(this.currentVersion);
        }
    }
}
