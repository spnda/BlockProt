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
package de.sean.blockprot.bukkit;

import com.google.common.collect.Sets;
import de.sean.blockprot.bukkit.commands.BlockProtCommand;
import de.sean.blockprot.bukkit.config.DefaultConfig;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.integrations.TownyIntegration;
import de.sean.blockprot.bukkit.listeners.*;
import de.sean.blockprot.bukkit.tasks.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BlockProt extends JavaPlugin {
    /**
     * The bStats plugin ID.
     */
    public static final int pluginId = 9999;

    /**
     * The name of the default language file, which is the english
     * translation file.
     */
    public static final String defaultLanguageFile = "translations_en.yml";

    @Nullable
    private static BlockProt instance;

    @Nullable
    private static DefaultConfig defaultConfig = null;
    private final ArrayList<PluginIntegration> integrations = new ArrayList<>();
    protected Metrics metrics;

    /**
     * Get the current instance of the {@link BlockProt} plugin.
     *
     * @return The instance, or null if not enabled yet.
     */
    @Nullable
    public static BlockProt getInstance() {
        return instance;
    }

    /**
     * Gets the default config. Might throw an {@link AssertionError} if
     * the config is null.
     *
     * @return The default config.
     */
    @NotNull
    public static DefaultConfig getDefaultConfig() throws AssertionError {
        assert defaultConfig != null : "default config should be null.";
        return defaultConfig;
    }

    /**
     * Gets a unmodifiable list of all registered {@link PluginIntegration}s.
     *
     * @return List of all registered integrations.
     */
    public List<PluginIntegration> getIntegrations() {
        return Collections.unmodifiableList(integrations);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        defaultConfig = new DefaultConfig(this.getConfig());

        this.loadTranslations(defaultConfig.getLanguageFile() == null
            ? defaultLanguageFile
            : defaultConfig.getLanguageFile());

        /* Check for updates */
        Bukkit.getScheduler().runTaskAsynchronously(this, new UpdateChecker(new ArrayList<>(), this.getDescription()));

        /* bStats Metrics */
        metrics = new Metrics(this, pluginId);

        /* Register Listeners */
        final PluginManager pm = Bukkit.getServer().getPluginManager();
        registerEvent(pm, new BlockEventListener(this));
        registerEvent(pm, new ExplodeEventListener());
        registerEvent(pm, new HopperEventListener());
        registerEvent(pm, new InteractEventListener());
        registerEvent(pm, new InventoryEventListener());
        registerEvent(pm, new JoinEventListener());
        registerEvent(pm, new PistonEventListener());
        registerEvent(pm, new RedstoneEventListener());

        registerCommand("blockprot", new BlockProtCommand());

        if (getPlugin("Towny") != null)
            registerIntegration(new TownyIntegration());

        super.onEnable();
    }

    private void registerEvent(@NotNull PluginManager pm, Listener listener) {
        pm.registerEvents(listener, this);
    }

    private void registerCommand(String name, TabExecutor executor) {
        Objects.requireNonNull(this.getCommand(name)).setExecutor(executor);
    }

    private void registerIntegration(@NotNull PluginIntegration integration) {
        integration.load();
        this.integrations.add(integration);
    }

    /**
     * Get a plugin by string ID from Bukkit's {@link PluginManager}.
     *
     * @param pluginName The ID of the plugin to get.
     * @return The main plugin instance or null if not found.
     * @since 0.4.0
     */
    @Nullable
    public Plugin getPlugin(String pluginName) {
        return this.getServer().getPluginManager().getPlugin(pluginName);
    }

    /**
     * Load the translations from the data folder.
     *
     * @param fileName The name of the translations file.
     */
    private void loadTranslations(String fileName) {
        final String langFolder = "lang/";

        // Ensure that all translation files have been saved properly.
        // For now, we will simply hard code these values.
        for (String resource : Sets.newHashSet("translations_de.yml", "translations_en.yml", "translations_es.yml", "translations_tr.yml")) {
            if ((new File(this.getDataFolder(), langFolder + resource)).exists()) continue;
            this.saveResource(langFolder + resource, false);
        }

        // Get the default language file. We specifically use the InputStream
        // if we cannot find the file in the data folder, to avoid having to
        // interact with edited files.
        File defLangFile = new File(this.getDataFolder(), langFolder + defaultLanguageFile);
        YamlConfiguration defaultConfig;
        if (!defLangFile.exists()) {
            InputStream defaultConfigStream = this.getResource(langFolder + defaultLanguageFile);

            // The JAR has been modified and there are files missing.
            if (defaultConfigStream == null) {
                throw new RuntimeException("Failed to get default language file. Possibly corrupt plugin?");
            }
            defaultConfig = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(defaultConfigStream)));
        } else {
            defaultConfig = YamlConfiguration.loadConfiguration(defLangFile);
        }

        // Get the wanted language file and load its config.
        // Load the configurations and initialize the Translator.
        YamlConfiguration wantedConfig = saveAndLoadConfigFile(langFolder, fileName, false);
        Translator.loadFromConfigs(defaultConfig, wantedConfig);
    }

    /**
     * Saves a config file and reads it. Relative to {@link JavaPlugin#getDataFolder()}.
     *
     * @param folder  The name of the folder where the file is located.
     * @param name    The name of the resource.
     * @param replace Whether or not to replace the file if it already exists.
     * @return The YamlConfiguration.
     */
    @NotNull
    public YamlConfiguration saveAndLoadConfigFile(String folder, String name, boolean replace) {
        final String path = folder + (folder.endsWith("/") ? "" : "/") + name;
        File file = new File(this.getDataFolder(), path);
        if (!file.exists())
            this.saveResource(path, replace);
        return YamlConfiguration.loadConfiguration(file);
    }
}
