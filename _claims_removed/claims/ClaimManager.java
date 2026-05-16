package de.sean.blockprot.bukkit.claims;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.BlockProtLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all player claims. Persists to {@code claims.yml} in the plugin data folder.
 *
 * <p>Lifecycle: call {@link #init(File)} once in {@code onEnable}, then use {@link #getInstance()}.</p>
 *
 * <h3>SP26 default flags</h3>
 * <p>When claims are loaded from disk, a missing YAML flag defaults to
 * {@code true} (ALLOW), matching the behavior for newly created claims.</p>
 *
 * @since SP26
 */
public class ClaimManager {

    @Nullable
    private static ClaimManager instance;

    private final File claimsFile;
    private final List<Claim> claims = new ArrayList<>();

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    private ClaimManager(@NotNull File dataFolder) {
        this.claimsFile = new File(dataFolder, "claims.yml");
        load();
    }

    public static void init(@NotNull File dataFolder) {
        instance = new ClaimManager(dataFolder);
    }

    @NotNull
    public static ClaimManager getInstance() {
        assert instance != null : "ClaimManager has not been initialised yet.";
        return instance;
    }

    // ─── Persistence ─────────────────────────────────────────────────────────

    private void load() {
        if (!claimsFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(claimsFile);
        ConfigurationSection root = config.getConfigurationSection("claims");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            try {
                ConfigurationSection cs = root.getConfigurationSection(key);
                if (cs == null) continue;

                UUID id      = UUID.fromString(key);
                String owner = Objects.requireNonNull(cs.getString("owner"));
                String name  = cs.getString("name", id.toString().substring(0, 8));
                String world = Objects.requireNonNull(cs.getString("world"));
                int minX = cs.getInt("minX"), minY = cs.getInt("minY"), minZ = cs.getInt("minZ");
                int maxX = cs.getInt("maxX"), maxY = cs.getInt("maxY"), maxZ = cs.getInt("maxZ");

                Map<ClaimFlag, Boolean> flags = new EnumMap<>(ClaimFlag.class);
                for (ClaimFlag f : ClaimFlag.values()) {
                    // SP26: default true (ALLOW) for backwards-compat with old claims lacking flag section
                    flags.put(f, cs.getBoolean("flags." + f.name().toLowerCase(), true));
                }

                String welcome = cs.getString("welcome", null);

                claims.add(new Claim(id, owner, name, world, minX, minY, minZ, maxX, maxY, maxZ, flags, welcome));
            } catch (Exception e) {
                BlockProt.getInstance().getLogger().warning(
                    "[Claims] Failed to load claim '" + key + "': " + e.getMessage());
                BlockProtLogger.log("[Claims] Load error for " + key + ": " + e);
            }
        }

        BlockProtLogger.log("[Claims] Loaded " + claims.size() + " claim(s) from disk.");
    }

    /** Saves all claims synchronously. Use {@link #saveAsync()} from the server main thread. */
    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (Claim c : claims) {
            String base = "claims." + c.getId();
            config.set(base + ".owner",  c.getOwnerUuid());
            config.set(base + ".name",   c.getName());
            config.set(base + ".world",  c.getWorldName());
            config.set(base + ".minX",   c.getMinX());
            config.set(base + ".minY",   c.getMinY());
            config.set(base + ".minZ",   c.getMinZ());
            config.set(base + ".maxX",   c.getMaxX());
            config.set(base + ".maxY",   c.getMaxY());
            config.set(base + ".maxZ",   c.getMaxZ());
            for (ClaimFlag f : ClaimFlag.values()) {
                config.set(base + ".flags." + f.name().toLowerCase(), c.getFlag(f));
            }
            if (c.getWelcomeMessage() != null) {
                config.set(base + ".welcome", c.getWelcomeMessage());
            }
        }

        try {
            config.save(claimsFile);
        } catch (IOException e) {
            BlockProt.getInstance().getLogger().severe(
                "[Claims] Failed to save claims.yml: " + e.getMessage());
        }
    }

    /** Schedules an async save. Must be called from the server main thread. */
    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(BlockProt.getInstance(), this::save);
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    /**
     * Tries to create a named claim between {@code posA} and {@code posB}.
     *
     * @param ownerUuid UUID string of the owning player.
     * @param name      Human-readable name for the claim.
     * @param posA      First corner selected with the wand.
     * @param posB      Second corner selected with the wand.
     * @return The new {@link Claim}, or {@code null} if it overlaps another player's claim.
     */
    @Nullable
    public Claim createClaim(@NotNull String ownerUuid, @NotNull String name,
                             @NotNull Location posA, @NotNull Location posB) {
        Claim candidate = new Claim(UUID.randomUUID(), ownerUuid, name, posA, posB);

        for (Claim existing : claims) {
            if (!existing.getOwnerUuid().equals(ownerUuid) && existing.overlaps(candidate)) {
                return null; // Overlap with another player's territory
            }
        }

        claims.add(candidate);
        saveAsync();
        BlockProtLogger.log("[Claims] Created claim '" + name + "' (" + candidate.getId() + ") for " + ownerUuid);
        return candidate;
    }

    /**
     * Deletes the claim by ID if the requesting player is its owner.
     *
     * @return {@code true} if a claim was removed.
     */
    public boolean deleteClaim(@NotNull UUID claimId, @NotNull String requestingUuid) {
        boolean removed = claims.removeIf(c ->
            c.getId().equals(claimId) && c.getOwnerUuid().equals(requestingUuid));
        if (removed) {
            saveAsync();
            BlockProtLogger.log("[Claims] Deleted claim " + claimId);
        }
        return removed;
    }

    // ─── Lookups ─────────────────────────────────────────────────────────────

    /**
     * Returns the claim that contains {@code loc}, or {@code null}.
     * If multiple claims overlap (same owner), returns the smallest one (best match).
     */
    @Nullable
    public Claim getClaimAt(@Nullable Location loc) {
        if (loc == null) return null;
        Claim best = null;
        long bestVol = Long.MAX_VALUE;
        for (Claim c : claims) {
            if (c.contains(loc) && c.getVolume() < bestVol) {
                best = c;
                bestVol = c.getVolume();
            }
        }
        return best;
    }

    /** Returns all claims that contain the given location (may be multiple for same owner). */
    @NotNull
    public List<Claim> getAllClaimsAt(@Nullable Location loc) {
        if (loc == null) return Collections.emptyList();
        List<Claim> result = new ArrayList<>();
        for (Claim c : claims) {
            if (c.contains(loc)) result.add(c);
        }
        return result;
    }

    @Nullable
    public Claim getClaimById(@NotNull UUID id) {
        for (Claim c : claims) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    @Nullable
    public Claim getClaimByName(@NotNull String ownerUuid, @NotNull String name) {
        for (Claim c : claims) {
            if (c.getOwnerUuid().equals(ownerUuid) && c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    @NotNull
    public List<Claim> getClaimsFor(@NotNull String ownerUuid) {
        List<Claim> result = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getOwnerUuid().equals(ownerUuid)) result.add(c);
        }
        return result;
    }

    /** Returns true if the given name is already in use by this owner. */
    public boolean nameExists(@NotNull String ownerUuid, @NotNull String name) {
        for (Claim c : claims) {
            if (c.getOwnerUuid().equals(ownerUuid) && c.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
