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
     * Gets a {@link ArrayList} of all registered {@link PluginIntegration}.
     *
     * @return List of all registered integrations.
     */
    public List<PluginIntegration> getIntegrations() {
        return integrations;
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
        // Get the default language file. We specifically use the InputStream
        // if we cannot find the file in the data folder, to avoid having to
        // interact with edited files.
        File defLangFile = new File(this.getDataFolder(), defaultLanguageFile);
        YamlConfiguration defaultConfig;
        if (!defLangFile.exists()) {
            InputStream defaultConfigStream = this.getResource(defaultLanguageFile);
            // The JAR has been modified and there are files missing.
            assert defaultConfigStream != null;
            defaultConfig = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(defaultConfigStream)));
        } else {
            defaultConfig = YamlConfiguration.loadConfiguration(defLangFile);
        }

        // Get the wanted language file.
        File langFile = new File(this.getDataFolder(), fileName);
        if (!langFile.exists()) {
            this.saveResource(fileName, true);
            langFile = new File(this.getDataFolder(), fileName);
        }

        // Load the configurations and initialize the Translator.
        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        Translator.loadFromConfigs(defaultConfig, config);
    }

    /**
     * Saves a config file and reads it.
     *
     * @param path    The path of the resource.
     * @param replace Whether or not to replace the file if it already exists.
     * @return The YamlConfiguration.
     */
    @NotNull
    public YamlConfiguration saveAndLoadConfigFile(String path, boolean replace) {
        File file = new File(this.getDataFolder(), path);
        if (!file.exists()) {
            InputStream resource = this.getResource(path);
            if (resource != null)
                this.saveResource(path, replace);
            else
                throw new RuntimeException("Failed to save resource at: " + path);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
