package de.sean.blockprot;

import de.sean.blockprot.bukkit.commands.BlockProtCommand;
import de.sean.blockprot.bukkit.integrations.PluginIntegration;
import de.sean.blockprot.bukkit.listeners.*;
import de.sean.blockprot.bukkit.tasks.UpdateChecker;
import de.sean.blockprot.config.DefaultConfig;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockProt extends JavaPlugin {
    public static final int pluginId = 9999;
    public static final String defaultLanguageFile = "translations_en.yml";

    private static BlockProt instance;
    private static DefaultConfig defaultConfig;

    protected Metrics metrics;
    private final ArrayList<PluginIntegration> integrations = new ArrayList<>();

    public static BlockProt getInstance() {
        return instance;
    }

    public static DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

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
        Bukkit.getScheduler().runTaskAsynchronously(this, new UpdateChecker(new ArrayList<Player>(), this.getDescription()));

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
        registerEvent(pm, new RedstoneEventListener());

        registerCommand("blockprot", new BlockProtCommand());

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
     * @param fileName The name of the translations file. Defaults to
     *                 {@link BlockProt#defaultLanguageFile} if it can't be found.
     */
    private void loadTranslations(String fileName) {
        File file = new File(this.getDataFolder(), fileName);
        /* Only save the resource if it exists inside the JAR and it has not
         * been saved to the dataFolder yet. */
        InputStream resource = this.getResource(fileName);
        if (resource == null && !file.exists()) {
            Bukkit.getLogger().warning("Could not find language file: $fileName. Defaulting to $defaultLanguageFile");
            this.saveResource(defaultLanguageFile, true);
            file = new File(this.getDataFolder(), defaultLanguageFile);
        } else if (resource != null && !file.exists()) {
            this.saveResource(fileName, true);
            if (!file.exists()) throw new RuntimeException("Could not load language file: $fileName");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Translator.loadFromConfig(config);
    }

    /**
     * Saves a config file and reads it.
     * @param path The path of the resource.
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
