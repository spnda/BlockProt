package de.sean.blockprot.bukkit.claims;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a named 3D protected region owned by a single player.
 *
 * <h3>Valores por defecto de flags (SP26)</h3>
 * <p>Al crear una parcela nueva, todas las flags (TNT, Fuego, PvP, Redstone)
 * se establecen como {@code true} (ALLOW) para que el dueño no tenga que
 * configurarlas manualmente desde el primer momento.</p>
 *
 * @since SP26
 */
public class Claim {

    private final UUID id;
    private final String ownerUuid;
    private final String worldName;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private final Map<ClaimFlag, Boolean> flags;

    /** Human-readable name chosen by the owner at creation time. */
    @NotNull
    private String name;

    /**
     * Optional welcome message shown to players when they enter this claim.
     * Supports legacy colour codes (§). {@code null} means no message.
     */
    @Nullable
    private String welcomeMessage;

    // ─── Constructor: new claim from wand positions ──────────────────────────

    /**
     * Crea una nueva parcela a partir de las dos esquinas seleccionadas con la varita.
     *
     * <p>Todas las flags se inicializan a {@code true} (ALLOW) para que el jugador
     * no tenga que configurarlas manualmente.</p>
     */
    public Claim(@NotNull UUID id, @NotNull String ownerUuid, @NotNull String name,
                 @NotNull Location posA, @NotNull Location posB) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.name = name.trim().isEmpty() ? id.toString().substring(0, 8) : name.trim();
        this.worldName = posA.getWorld().getName();
        this.minX = Math.min(posA.getBlockX(), posB.getBlockX());
        this.minY = Math.min(posA.getBlockY(), posB.getBlockY());
        this.minZ = Math.min(posA.getBlockZ(), posB.getBlockZ());
        this.maxX = Math.max(posA.getBlockX(), posB.getBlockX());
        this.maxY = Math.max(posA.getBlockY(), posB.getBlockY());
        this.maxZ = Math.max(posA.getBlockZ(), posB.getBlockZ());
        this.flags = new EnumMap<>(ClaimFlag.class);
        // SP26: all flags start ALLOW (true) for player convenience
        for (ClaimFlag f : ClaimFlag.values()) flags.put(f, true);
        this.welcomeMessage = null;
    }

    // ─── Constructor: deserialised from claims.yml ───────────────────────────

    public Claim(@NotNull UUID id, @NotNull String ownerUuid, @NotNull String name,
                 @NotNull String worldName,
                 int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                 @NotNull Map<ClaimFlag, Boolean> flags,
                 @Nullable String welcomeMessage) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.flags = new EnumMap<>(flags);
        this.welcomeMessage = welcomeMessage;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public @NotNull UUID getId()          { return id; }
    public @NotNull String getOwnerUuid() { return ownerUuid; }
    public @NotNull String getName()      { return name; }
    public @NotNull String getWorldName() { return worldName; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    /** Returns the welcome message for this claim, or {@code null} if none is set. */
    public @Nullable String getWelcomeMessage() { return welcomeMessage; }

    /** Sets the welcome message. Use {@code null} or an empty string to clear it. */
    public void setWelcomeMessage(@Nullable String welcomeMessage) {
        this.welcomeMessage = (welcomeMessage != null && welcomeMessage.isEmpty()) ? null : welcomeMessage;
    }

    /** Returns the volume of this claim in blocks (length × height × width). */
    public long getVolume() {
        return (long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    // ─── Spatial checks ──────────────────────────────────────────────────────

    /** Returns true if the given location falls inside this claim's 3D bounding box. */
    public boolean contains(@Nullable Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(worldName)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    /** Returns true if this claim's bounding box overlaps with {@code other}'s. */
    public boolean overlaps(@NotNull Claim other) {
        if (!other.worldName.equals(this.worldName)) return false;
        return !(other.maxX < minX || other.minX > maxX
              || other.maxY < minY || other.minY > maxY
              || other.maxZ < minZ || other.minZ > maxZ);
    }

    // ─── Flag access ─────────────────────────────────────────────────────────

    /**
     * Obtiene el valor de una flag.
     *
     * <p>Para parcelas nuevas todas son {@code true}; para parcelas cargadas desde
     * disco el valor guardado tiene precedencia (con {@code true} como default si
     * la key falta, manteniendo compatibilidad hacia atrás).</p>
     */
    public boolean getFlag(@NotNull ClaimFlag flag) {
        return flags.getOrDefault(flag, true);
    }

    public void setFlag(@NotNull ClaimFlag flag, boolean value) {
        flags.put(flag, value);
    }

    public @NotNull Map<ClaimFlag, Boolean> getFlags() {
        return Collections.unmodifiableMap(flags);
    }
}
