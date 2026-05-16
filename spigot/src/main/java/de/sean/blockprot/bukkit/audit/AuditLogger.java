package de.sean.blockprot.bukkit.audit;

import de.sean.blockprot.bukkit.BlockProt;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// SQLite-based access audit log. Records who attempted to access which block and when.
// Writes are always async. Reads may be sync or async depending on context.
public final class AuditLogger {

    public enum Action {
        ACCESS_DENIED,   // A player without permission attempted to open a protected block.
        ACCESS_GRANTED   // A player with permission accessed a block (optional, disabled by default).
    }

    public record AuditEntry(
        long id,
        String playerUuid,
        String playerName,
        String world,
        int x, int y, int z,
        Action action,
        long timestamp
    ) {}

    private static final String DB_NAME = "blockprot_audit.sqlite";
    private static final int MAX_ENTRIES = 50_000;

    private final Connection connection;

    public AuditLogger(@NotNull File dataFolder) throws SQLException {
        File db = new File(dataFolder, DB_NAME);
        connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        connection.setAutoCommit(true);
        initSchema();
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    player_name TEXT,
                    world       TEXT NOT NULL,
                    x           INTEGER NOT NULL,
                    y           INTEGER NOT NULL,
                    z           INTEGER NOT NULL,
                    action      TEXT NOT NULL,
                    timestamp   INTEGER NOT NULL
                )
                """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_location ON audit_log(world, x, y, z)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player   ON audit_log(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_time     ON audit_log(timestamp)");
        }
    }

    // Records an event asynchronously.
    public void log(@NotNull UUID player, @NotNull String playerName, @NotNull Location loc, @NotNull Action action) {
        CompletableFuture.runAsync(() -> {
            try {
                pruneIfNeeded();
                try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO audit_log(player_uuid,player_name,world,x,y,z,action,timestamp) VALUES(?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, player.toString());
                    ps.setString(2, playerName);
                    ps.setString(3, loc.getWorld() != null ? loc.getWorld().getName() : "unknown");
                    ps.setInt(4, loc.getBlockX());
                    ps.setInt(5, loc.getBlockY());
                    ps.setInt(6, loc.getBlockZ());
                    ps.setString(7, action.name());
                    ps.setLong(8, System.currentTimeMillis());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                BlockProt.getInstance().getLogger().warning("[Audit] Failed to save event: " + e.getMessage());
            }
        });
    }

    @NotNull
    public List<AuditEntry> getEntriesForBlock(@NotNull String world, int x, int y, int z, int limit) {
        List<AuditEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT * FROM audit_log WHERE world=? AND x=? AND y=? AND z=? ORDER BY timestamp DESC LIMIT ?")) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            ps.setInt(5, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) entries.add(mapRow(rs));
        } catch (SQLException e) {
            BlockProt.getInstance().getLogger().warning("[Audit] Failed to read events by block: " + e.getMessage());
        }
        return entries;
    }

    @NotNull
    public List<AuditEntry> getEntriesForPlayer(@NotNull UUID player, int limit) {
        List<AuditEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT * FROM audit_log WHERE player_uuid=? ORDER BY timestamp DESC LIMIT ?")) {
            ps.setString(1, player.toString());
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) entries.add(mapRow(rs));
        } catch (SQLException e) {
            BlockProt.getInstance().getLogger().warning("[Audit] Failed to read events by player: " + e.getMessage());
        }
        return entries;
    }

    private AuditEntry mapRow(ResultSet rs) throws SQLException {
        return new AuditEntry(
            rs.getLong("id"),
            rs.getString("player_uuid"),
            rs.getString("player_name"),
            rs.getString("world"),
            rs.getInt("x"), rs.getInt("y"), rs.getInt("z"),
            Action.valueOf(rs.getString("action")),
            rs.getLong("timestamp")
        );
    }

    private void pruneIfNeeded() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM audit_log");
            if (rs.next() && rs.getInt(1) > MAX_ENTRIES) {
                stmt.execute("DELETE FROM audit_log WHERE id IN (SELECT id FROM audit_log ORDER BY timestamp ASC LIMIT 5000)");
            }
        }
    }

    public void close() {
        try {
            if (!connection.isClosed()) connection.close();
        } catch (SQLException ignored) {}
    }
}
