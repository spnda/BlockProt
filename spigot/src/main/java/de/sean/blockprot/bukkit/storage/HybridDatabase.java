package de.sean.blockprot.bukkit.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.TranslationKey;
import de.sean.blockprot.bukkit.Translator;
import de.sean.blockprot.bukkit.config.DefaultConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Optional MySQL/MariaDB index for BlockProt.
 *
 * <p>NBT remains the source of truth for per-block ownership and friends. This
 * database only mirrors block locations and player global trust lists so large
 * servers can audit and search without scanning world region files.</p>
 */
public final class HybridDatabase {

    private final BlockProt plugin;
    private final ConcurrentMap<String, Set<String>> globalTrustCache = new ConcurrentHashMap<>();

    private HikariDataSource dataSource;
    private volatile boolean enabled;

    public HybridDatabase(@NotNull BlockProt plugin) {
        this.plugin = plugin;
    }

    public void start(@NotNull DefaultConfig config) {
        if (!config.isMysqlEnabled()) {
            enabled = false;
            return;
        }

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("BlockProt-Hybrid");
        hikari.setJdbcUrl(config.getMysqlJdbcUrl());
        hikari.setUsername(config.getMysqlUsername());
        hikari.setPassword(config.getMysqlPassword());
        hikari.setMaximumPoolSize(config.getMysqlPoolSize());
        hikari.setMinimumIdle(config.getMysqlMinimumIdle());
        hikari.setConnectionTimeout(config.getMysqlConnectionTimeoutMillis());
        hikari.setInitializationFailTimeout(-1);
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(hikari);
            enabled = true;
            runAsync(this::initSchema);
            runAsync(this::loadGlobalTrustCache);
            plugin.getLogger().info(Translator.get(TranslationKey.CONSOLE__MYSQL_ENABLED));
        } catch (RuntimeException e) {
            enabled = false;
            plugin.getLogger().warning(Translator.get(TranslationKey.CONSOLE__MYSQL_DISABLED)
                .replace("{error}", e.getMessage()));
        }
    }

    public boolean isEnabled() {
        return enabled && dataSource != null && !dataSource.isClosed();
    }

    public void close() {
        enabled = false;
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
        globalTrustCache.clear();
    }

    public void upsertBlockIndex(@NotNull UUID owner, @NotNull Location location, @NotNull String blockType) {
        if (!isEnabled() || location.getWorld() == null) return;

        runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("""
                     INSERT INTO blockprot_block_index(owner_uuid, world, x, y, z, block_type, updated_at)
                     VALUES(?,?,?,?,?,?,?)
                     ON DUPLICATE KEY UPDATE
                         owner_uuid = VALUES(owner_uuid),
                         block_type = VALUES(block_type),
                         updated_at = VALUES(updated_at)
                     """)) {
                ps.setString(1, owner.toString());
                ps.setString(2, location.getWorld().getName());
                ps.setInt(3, location.getBlockX());
                ps.setInt(4, location.getBlockY());
                ps.setInt(5, location.getBlockZ());
                ps.setString(6, blockType);
                ps.setLong(7, System.currentTimeMillis());
                ps.executeUpdate();
            }
        });
    }

    public void removeBlockIndex(@NotNull Location location) {
        if (!isEnabled() || location.getWorld() == null) return;

        runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM blockprot_block_index WHERE world=? AND x=? AND y=? AND z=?")) {
                ps.setString(1, location.getWorld().getName());
                ps.setInt(2, location.getBlockX());
                ps.setInt(3, location.getBlockY());
                ps.setInt(4, location.getBlockZ());
                ps.executeUpdate();
            }
        });
    }

    public void addGlobalTrust(@NotNull UUID owner, @NotNull UUID friend) {
        addToCache(owner.toString(), friend.toString());
        if (!isEnabled()) return;

        runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement("""
                     INSERT INTO blockprot_global_trust(owner_uuid, friend_uuid, updated_at)
                     VALUES(?,?,?)
                     ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at)
                     """)) {
                ps.setString(1, owner.toString());
                ps.setString(2, friend.toString());
                ps.setLong(3, System.currentTimeMillis());
                ps.executeUpdate();
            }
        });
    }

    public void removeGlobalTrust(@NotNull UUID owner, @NotNull UUID friend) {
        Set<String> cached = globalTrustCache.get(owner.toString());
        if (cached != null) cached.remove(friend.toString());
        if (!isEnabled()) return;

        runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM blockprot_global_trust WHERE owner_uuid=? AND friend_uuid=?")) {
                ps.setString(1, owner.toString());
                ps.setString(2, friend.toString());
                ps.executeUpdate();
            }
        });
    }

    @NotNull
    public Set<String> getCachedGlobalTrust(@NotNull UUID owner) {
        return globalTrustCache.getOrDefault(owner.toString(), Collections.emptySet());
    }

    @NotNull
    public CompletableFuture<Set<String>> getGlobalTrustAsync(@NotNull UUID owner) {
        Set<String> cached = globalTrustCache.get(owner.toString());
        if (cached != null) {
            return CompletableFuture.completedFuture(new HashSet<>(cached));
        }

        CompletableFuture<Set<String>> future = new CompletableFuture<>();
        if (!isEnabled()) {
            future.complete(Collections.emptySet());
            return future;
        }

        runAsync(() -> {
            Set<String> loaded = new HashSet<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                     "SELECT friend_uuid FROM blockprot_global_trust WHERE owner_uuid=?")) {
                ps.setString(1, owner.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) loaded.add(rs.getString("friend_uuid"));
                }
                globalTrustCache.put(owner.toString(), ConcurrentHashMap.newKeySet());
                globalTrustCache.get(owner.toString()).addAll(loaded);
                future.complete(loaded);
            } catch (SQLException e) {
                plugin.getLogger().warning(Translator.get(TranslationKey.CONSOLE__MYSQL_LOOKUP_FAILED)
                    .replace("{error}", e.getMessage()));
                future.complete(Collections.emptySet());
            }
        });
        return future;
    }

    private void initSchema() {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS blockprot_global_trust (
                    owner_uuid VARCHAR(36) NOT NULL,
                    friend_uuid VARCHAR(36) NOT NULL,
                    updated_at BIGINT NOT NULL,
                    PRIMARY KEY(owner_uuid, friend_uuid),
                    INDEX idx_friend_uuid(friend_uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS blockprot_block_index (
                    owner_uuid VARCHAR(36) NOT NULL,
                    world VARCHAR(128) NOT NULL,
                    x INT NOT NULL,
                    y INT NOT NULL,
                    z INT NOT NULL,
                    block_type VARCHAR(64) NOT NULL,
                    updated_at BIGINT NOT NULL,
                    PRIMARY KEY(world, x, y, z),
                    INDEX idx_owner_uuid(owner_uuid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        } catch (SQLException e) {
            plugin.getLogger().warning(Translator.get(TranslationKey.CONSOLE__MYSQL_SCHEMA_FAILED)
                .replace("{error}", e.getMessage()));
        }
    }

    private void loadGlobalTrustCache() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                 "SELECT owner_uuid, friend_uuid FROM blockprot_global_trust");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                addToCache(rs.getString("owner_uuid"), rs.getString("friend_uuid"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning(Translator.get(TranslationKey.CONSOLE__MYSQL_CACHE_LOAD_FAILED)
                .replace("{error}", e.getMessage()));
        }
    }

    private void addToCache(@NotNull String owner, @NotNull String friend) {
        globalTrustCache.computeIfAbsent(owner, ignored -> ConcurrentHashMap.newKeySet()).add(friend);
    }

    private void runAsync(@NotNull SqlRunnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                runnable.run();
            } catch (SQLException e) {
                plugin.getLogger().warning(Translator.get(TranslationKey.CONSOLE__MYSQL_OPERATION_FAILED)
                    .replace("{error}", e.getMessage()));
            }
        });
    }

    @FunctionalInterface
    private interface SqlRunnable {
        void run() throws SQLException;
    }
}
