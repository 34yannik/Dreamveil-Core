package de.yannik.dreamveilCore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.yannik.dreamveilCore.DreamveilCore;
import de.yannik.dreamveilCore.util.Log;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static HikariDataSource dataSource;
    private static DreamveilCore plugin;
    private static FileConfiguration pluginCfg;

    public Database(DreamveilCore plugin) {
        Database.plugin = plugin;
        pluginCfg = plugin.getConfig();

        Log.info("Initializing database connection...");
        setupDataSource();
    }

    private void setupDataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" +
                pluginCfg.getString("database.host") + "/" +
                pluginCfg.getString("database.name"));
        config.setUsername(pluginCfg.getString("database.user"));
        config.setPassword(pluginCfg.getString("database.password"));

        config.setPoolName(pluginCfg.getString("hikari.poolName"));
        config.setMaximumPoolSize(pluginCfg.getInt("hikari.maximumPoolSize"));
        config.setMinimumIdle(pluginCfg.getInt("hikari.minimumIdle"));
        config.setConnectionTimeout(pluginCfg.getLong("hikari.connectionTimeout"));
        config.setIdleTimeout(pluginCfg.getLong("hikari.idleTimeout"));
        config.setMaxLifetime(pluginCfg.getLong("hikari.maxLifetime"));
        config.setLeakDetectionThreshold(pluginCfg.getLong("hikari.leakDetectionThreshold"));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            Log.info("Database connection established successfully.");
        } catch (SQLException e) {
            Log.error("Database connection failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            Log.error("Tried to access database but DataSource is not available.");
            throw new SQLException("Datenquelle nicht verfügbar");
        }
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            Log.info("Database connection pool closed.");
        }
    }

    public void initializeDatabase() throws SQLException {
        Log.info("Initializing database tables...");

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            connection.setAutoCommit(false);

            String[] tables = {

                    /* ── CORE ─────────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS player_core (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "username VARCHAR(16) NOT NULL, " +
                            "first_login DATETIME, " +
                            "last_login DATETIME, " +
                            "last_logout DATETIME" +
                            ")",

                    /* ── ECONOMY ──────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS player_economy (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "balance BIGINT DEFAULT 250, " +
                            "shards BIGINT DEFAULT 0" +
                            ")",

                    /* ── ACTIVITY ─────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS player_activity (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "playtime BIGINT DEFAULT 0, " +
                            "login_streak INT DEFAULT 0, " +
                            "longest_streak INT DEFAULT 0" +
                            ")",

                    /* ── RANKS ────────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS player_ranks (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "player_rank VARCHAR(32) NOT NULL, " +
                            "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "expires_at DATETIME NULL, " +
                            "granted_by VARCHAR(64) NOT NULL, " +
                            "UNIQUE KEY unique_rank_per_player (uuid, player_rank), " +
                            "INDEX idx_uuid (uuid), " +
                            "INDEX idx_expires (expires_at)" +
                            ")",

                    /* ── SETTINGS  (includes color columns from start) ─────────── */
                    "CREATE TABLE IF NOT EXISTS player_settings (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "pronouns VARCHAR(32) DEFAULT ''," +
                            "show_pronouns BOOLEAN DEFAULT TRUE," +
                            "selected_title VARCHAR(64) DEFAULT NULL," +
                            "chat_msg_color VARCHAR(64)  DEFAULT NULL," +
                            "chat_name_color VARCHAR(64) DEFAULT NULL" +
                            ")",

                    /* ── TITLES ───────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS player_titles (" +
                            "uuid VARCHAR(36) NOT NULL," +
                            "title VARCHAR(64) NOT NULL," +
                            "unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (uuid, title)" +
                            ")",

                    /* ── RANK LOGS ────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS rank_logs (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "player_name VARCHAR(16) NOT NULL, " +
                            "player_rank VARCHAR(32) NOT NULL, " +
                            "action VARCHAR(32) NOT NULL, " +
                            "duration_days BIGINT DEFAULT -1, " +
                            "granted_by VARCHAR(64) NOT NULL, " +
                            "logged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "INDEX idx_uuid (uuid), " +
                            "INDEX idx_logged_at (logged_at)" +
                            ")",

                    /* ── UNLOCKED COLORS ──────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS player_unlocked_colors (" +
                            "uuid VARCHAR(36) NOT NULL," +
                            "color_id VARCHAR(64) NOT NULL," +
                            "unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (uuid, color_id)," +
                            "INDEX idx_uc_uuid (uuid)" +
                            ")",

                    /* ── FAMILIES ─────────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS families (" +
                            "id VARCHAR(36) PRIMARY KEY," +
                            "name VARCHAR(32) NOT NULL," +
                            "tag VARCHAR(5) NOT NULL," +
                            "owner_uuid VARCHAR(36) NOT NULL," +
                            "description VARCHAR(256) DEFAULT NULL," +
                            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "UNIQUE KEY unique_family_name (name)," +
                            "UNIQUE KEY unique_family_tag (tag)," +
                            "INDEX idx_fam_owner (owner_uuid)" +
                            ")",

                    /* ── FAMILY MEMBERS ───────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS family_members (" +
                            "family_id VARCHAR(36) NOT NULL," +
                            "player_uuid VARCHAR(36) NOT NULL," +
                            "player_name VARCHAR(16) NOT NULL," +
                            "role VARCHAR(16) NOT NULL DEFAULT 'MEMBER'," +
                            "joined_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (family_id, player_uuid)," +
                            "INDEX idx_fm_player (player_uuid)" +
                            ")",

                    /* ── FAMILY HOME ──────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS family_home (" +
                            "family_id VARCHAR(36) PRIMARY KEY," +
                            "world VARCHAR(64) NOT NULL," +
                            "x DOUBLE NOT NULL," +
                            "y DOUBLE NOT NULL," +
                            "z DOUBLE NOT NULL," +
                            "yaw FLOAT DEFAULT 0," +
                            "pitch FLOAT DEFAULT 0," +
                            "set_by VARCHAR(36) NOT NULL," +
                            "set_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                            ")",

                    /* ── FAMILY BUFFS ─────────────────────────────────────────── */
                    "CREATE TABLE IF NOT EXISTS family_buffs (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "family_id VARCHAR(36) NOT NULL," +
                            "buff_type VARCHAR(32) NOT NULL," +
                            "level INT NOT NULL DEFAULT 1," +
                            "expires_at DATETIME NULL," +
                            "activated_by VARCHAR(36) NOT NULL," +
                            "activated_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "INDEX idx_fb_family (family_id)," +
                            "INDEX idx_fb_expires (expires_at)" +
                            ")"
            };

            for (String query : tables) {
                statement.addBatch(query);
            }

            int[] results = statement.executeBatch();
            connection.commit();
            Log.info("Tables created/verified (" + results.length + " statements).");

        } catch (SQLException e) {
            Log.error("Database initialization failed: " + e.getMessage());
            throw e;
        } finally {
            try (Connection c = getConnection()) {
                c.setAutoCommit(true);
            }
        }

        // Run migrations for pre-existing installations
        runMigrations();
    }

    /**
     * Applies schema migrations for servers upgrading from an older version.
     * Each migration is idempotent: it checks whether the change is needed
     * before applying it.
     */
    private void runMigrations() {
        Log.info("Running schema migrations...");

        // Migration 1: Add color columns to player_settings
        migrateAddColumn("player_settings", "chat_msg_color",
                "ALTER TABLE player_settings ADD COLUMN chat_msg_color VARCHAR(64) DEFAULT NULL");
        migrateAddColumn("player_settings", "chat_name_color",
                "ALTER TABLE player_settings ADD COLUMN chat_name_color VARCHAR(64) DEFAULT NULL");

        // Migration 2: Add tag column to families
        migrateAddColumn("families", "tag",
                "ALTER TABLE families ADD COLUMN tag VARCHAR(5) NOT NULL DEFAULT 'N/A' AFTER name");
        migrateAddUniqueIndex("families", "unique_family_tag",
                "ALTER TABLE families ADD UNIQUE KEY unique_family_tag (tag)");

        Log.info("Schema migrations complete.");
    }

    /**
     * Add a column to a table only if it does not already exist.
     * Uses INFORMATION_SCHEMA for a reliable, version-independent check.
     */
    private void migrateAddColumn(String table, String column, String alterSql) {
        String checkSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME   = ?
                  AND COLUMN_NAME  = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {

            check.setString(1, table);
            check.setString(2, column);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }

            try (Statement alter = conn.createStatement()) {
                alter.executeUpdate(alterSql);
                Log.info("Migration: added column '" + column + "' to '" + table + "'.");
            }

        } catch (SQLException e) {
            Log.error("Migration failed (" + table + "." + column + "): " + e.getMessage());
        }
    }

    private void migrateAddUniqueIndex(String table, String indexName, String alterSql) {
        String checkSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME   = ?
                  AND INDEX_NAME   = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {

            check.setString(1, table);
            check.setString(2, indexName);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }

            try (Statement alter = conn.createStatement()) {
                alter.executeUpdate(alterSql);
                Log.info("Migration: added unique index '" + indexName + "' on '" + table + "'.");
            }

        } catch (SQLException e) {
            Log.error("Migration failed (index " + indexName + " on " + table + "): " + e.getMessage());
        }
    }
}