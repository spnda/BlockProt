/*
 * Copyright (C) 2021 - 2023 spnda
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
import de.sean.blockprot.bukkit.integrations.*;
import de.sean.blockprot.bukkit.listeners.*;
import de.sean.blockprot.bukkit.metrics.IntegrationBarChart;
import de.sean.blockprot.bukkit.nbt.StatHandler;
import de.sean.blockprot.bukkit.tasks.UpdateChecker;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.wesjd.anvilgui.version.VersionMatcher;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.enginehub.squirrelid.cache.ProfileCache;
import org.enginehub.squirrelid.cache.SQLiteCache;
import org.enginehub.squirrelid.resolver.ProfileService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The main plugin instance of BlockProt.
 */
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

    @Nullable
    private static SQLiteCache playerProfileCache = null;
    @Nullable
    private static ProfileService playerProfileService = null;

    private Metrics metrics;

    /**
     * Get the current instance of the {@link BlockProt} plugin.
     * Might throw a {@link AssertionError} if this plugin has not
     * been enabled yet.
     *
     * @return The instance.
     * @since 0.4.0
     */
    @NotNull
    public static BlockProt getInstance() {
        assert instance != null;
        return instance;
    }

    /**
     * Gets the default config. Might throw an {@link AssertionError} if
     * the config is null.
     *
     * @return The default config.
     * @since 0.4.0
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
     * @since 0.4.0
     */
    public List<PluginIntegration> getIntegrations() {
        return Collections.unmodifiableList(integrations);
    }

    @NotNull
    public static ProfileCache getProfileCache() {
        assert playerProfileCache != null;
        return playerProfileCache;
    }

    @NotNull
    public static ProfileService getProfileService() {
        assert playerProfileService != null;
        return playerProfileService;
    }

    @Override
    public void onLoad() {
        instance = this;

        try {
            playerProfileCache = new SQLiteCache(new File(Bukkit.getWorldContainer(), "blockprot_usercache.sqlite"));
            playerProfileService = new CachedProfileService(playerProfileCache);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open SQLite connection to usercache database", e);
        }

        try { registerIntegration(new WorldGuardIntegration());     } catch (NoClassDefFoundError ignored) {}
        try { registerIntegration(new TownyIntegration());          } catch (NoClassDefFoundError ignored) {}
        try { registerIntegration(new PlaceholderAPIIntegration()); } catch (NoClassDefFoundError ignored) {}
        try { registerIntegration(new LandsPluginIntegration());    } catch (NoClassDefFoundError ignored) {}

        for (PluginIntegration integration : integrations) {
            try {
                integration.load();
            } catch (NoClassDefFoundError ignored) {}
        }
    }

    @Override
    public void onEnable() {
        if (isRunningCraftBukkit()) {
            final var message = "This plugin does not support running on CraftBukkit servers! Please use any Spigot server instead!";
            getLogger().severe(message);
            getServer().getPluginManager().registerEvents(new ErrorEventListener(message), this);
            return;
        }

        /* Check for updates */
        Bukkit.getScheduler().runTaskAsynchronously(this, new UpdateChecker(this.getDescription()));

        // We'll try and get the AnvilGUI API to select the version wrapper. Should it fail, we display an error that
        // we do not support the current Minecraft version.
        try {
            new VersionMatcher().match();
        } catch (IllegalStateException e) {
            final var message = "This plugin does not support the current Minecraft version! Please check if there is a new update available.";
            getLogger().severe(message);
            getServer().getPluginManager().registerEvents(new ErrorEventListener(message), this);
            return;
        }

        MinecraftVersion.disableUpdateCheck();

        new BlockProtAPI(this); // Init the API.
        StatHandler.enable();
        this.saveDefaultConfig();
        this.reloadConfigAndTranslations();

        /* bStats Metrics */
        metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new IntegrationBarChart());

        /* Register Listeners */
        final PluginManager pm = getServer().getPluginManager();
        registerEvent(pm, new BlockEventListener(this));
        registerEvent(pm, new EntityEventListener());
        registerEvent(pm, new ExplodeEventListener());
        registerEvent(pm, new HopperEventListener());
        registerEvent(pm, new InteractEventListener());
        registerEvent(pm, new InventoryEventListener());
        registerEvent(pm, new JoinEventListener());
        registerEvent(pm, new PistonEventListener());
        registerEvent(pm, new RedstoneEventListener());

        Objects.requireNonNull(this.getCommand("blockprot"))
            .setExecutor(new BlockProtCommand());

        /* Enable all integrations */
        for (PluginIntegration integration : integrations) {
            try {
                integration.enable();
                if (integration.isEnabled()) {
                    getLogger().info(String.format("Enabled plugin integration for plugin with id '%s'", integration.name));
                }
            } catch (NoClassDefFoundError ignored) {}
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (!isRunningCraftBukkit()) {
            getLogger().info("Saving statistic file...");
            StatHandler.disable();
            getServer().getOnlinePlayers().forEach(HumanEntity::closeInventory);
        }
        super.onDisable();
    }

    /**
     * Reloads the config and the translation files (possibly changed through config).
     * 
     * @since 1.0.0
     */
    public void reloadConfigAndTranslations() {
        this.reloadConfig();
        defaultConfig = new DefaultConfig(this.getConfig());

        Translator.resetTranslations();
        Translator.DEFAULT_FALLBACK = defaultConfig.getTranslationFallbackString();

        final String langFolder = "lang/";

        // Ensure that all translation files have been saved properly.
        for (String resource : Translator.DEFAULT_TRANSLATION_FILES) {
            if ((new File(this.getDataFolder(), langFolder + resource)).exists()) continue;
            this.saveResource(langFolder + resource, defaultConfig.shouldReplaceTranslations());
        }

        // Get the default language file. We do not use the English translation in the plugin's
        // data folder anymore, in case that has not been updated yet due to a plugin update.
        // Instead, we always use the resource within the JAR as the default language file.
        InputStream defaultLanguageStream = this.getResource(langFolder + defaultLanguageFile);

        if (defaultLanguageStream == null) {
            // The JAR has been modified or is corrupt and the translation files are missing.
            throw new RuntimeException("Failed to get default language file. Possibly corrupt plugin?");
        }
        YamlConfiguration defaultLanguageConfig = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(defaultLanguageStream)));

        // Get the wanted language file and load its config.
        // Load the configurations and initialize the Translator.
        final String fileName = defaultConfig.getLanguageFile() == null
            ? defaultLanguageFile
            : defaultConfig.getLanguageFile();
        YamlConfiguration wantedConfig = saveAndLoadConfigFile(langFolder, fileName, BlockProt.defaultConfig.shouldReplaceTranslations());
        Translator.loadFromConfigs(defaultLanguageConfig, wantedConfig);

        for (PluginIntegration integration : integrations) {
            integration.reload();
        }
    }

    private void registerEvent(@NotNull PluginManager pm, Listener listener) {
        pm.registerEvents(listener, this);
    }

    void registerIntegration(@NotNull PluginIntegration integration) {
        this.integrations.add(integration);
        getLogger().info(String.format("Registered plugin integration for plugin with id '%s'", integration.name));
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
     * Saves a config file and reads it. Relative to {@link JavaPlugin#getDataFolder()}.
     *
     * @param folder  The name of the folder where the file is located.
     * @param name    The name of the resource.
     * @param replace Whether or not to replace the file if it already exists.
     * @return The YamlConfiguration.
     * @since 0.4.7
     */
    @NotNull
    public YamlConfiguration saveAndLoadConfigFile(String folder, String name, boolean replace) {
        final String path = folder + (folder.endsWith("/") ? "" : "/") + name;
        File file = new File(this.getDataFolder(), path);
        if (!file.exists()) {
            try {
                this.saveResource(path, replace);
            } catch (IllegalArgumentException e) {
                // The resource doesn't exist in the JAR. Therefore, the config file doesn't
                // exist, and we should return an empty config.
                getLogger().warning("The configured language file does not exist: " + name);
                return new YamlConfiguration();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private boolean isRunningCraftBukkit() {
        try {
            Class<?> clazz = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }
}
