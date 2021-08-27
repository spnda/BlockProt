package de.sean.blockprot.bukkit.integrations;

import de.sean.blockprot.bukkit.BlockProt;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIIntegration extends PluginIntegration {
    private BlockProtExpansion expansion;

    public PlaceholderAPIIntegration() {
        super("placeholderapi.yml");
    }

    @Override
    public boolean isEnabled() {
        return expansion.isRegistered();
    }

    @Override
    public void load() {
        final Plugin papi = getPlugin();
        if (papi == null || !papi.isEnabled()) return;

        this.expansion = new BlockProtExpansion();
        this.expansion.register();
    }

    @Override
    public @Nullable Plugin getPlugin() {
        return BlockProt.getInstance().getPlugin("PlaceholderAPI");
    }

    private static final class BlockProtExpansion extends PlaceholderExpansion {
        /**
         * Essentially a prefix to every placeholder we serve.
         * Cannot contain any underscores.
         */
        @Override
        public @NotNull String getIdentifier() {
            return "blockprot";
        }

        @Override
        public @NotNull String getAuthor() {
            return BlockProt.getInstance().getDescription().getAuthors().toString();
        }

        @Override
        public @NotNull String getVersion() {
            return BlockProt.getInstance().getDescription().getVersion();
        }
    }
}
