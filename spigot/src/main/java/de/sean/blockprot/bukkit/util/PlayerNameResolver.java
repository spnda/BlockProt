package de.sean.blockprot.bukkit.util;

import de.sean.blockprot.bukkit.BlockProt;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves Java and Floodgate/Geyser Bedrock names without forcing admins to
 * hard-code a single Bedrock prefix.
 */
public final class PlayerNameResolver {
    private PlayerNameResolver() {}

    @Nullable
    public static OfflinePlayer findOfflinePlayer(@NotNull String input) {
        for (String candidate : getNameCandidates(input)) {
            Player online = Bukkit.getPlayerExact(candidate);
            if (online != null) return online;

            OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(candidate);
            if (cached != null) return cached;
        }

        for (String candidate : getNameCandidates(input)) {
            @SuppressWarnings("deprecation")
            OfflinePlayer fallback = Bukkit.getOfflinePlayer(candidate);
            if (fallback.hasPlayedBefore()) return fallback;
        }
        return null;
    }

    @NotNull
    public static Set<String> getNameCandidates(@NotNull String input) {
        String trimmed = input.trim();
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        if (trimmed.isEmpty()) return candidates;

        candidates.add(trimmed);
        for (String prefix : BlockProt.getDefaultConfig().getBedrockUsernamePrefixes()) {
            if (prefix == null) continue;
            if (!prefix.isEmpty() && trimmed.startsWith(prefix)) {
                candidates.add(trimmed.substring(prefix.length()));
            } else if (!prefix.isEmpty()) {
                candidates.add(prefix + trimmed);
            }
        }
        return candidates;
    }
}
