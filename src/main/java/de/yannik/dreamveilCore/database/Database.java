package de.yannik.dreamveilCore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.yannik.dreamveilCore.DreamveilCore;
import de.yannik.dreamveilCore.util.Log;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
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

            String[] playerTables = {

                    /* ==================== */
                    /*        CORE          */
                    /* ==================== */
                    "CREATE TABLE IF NOT EXISTS player_core (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "username VARCHAR(16) NOT NULL, " +
                            "first_login DATETIME, " +
                            "last_login DATETIME, " +
                            "last_logout DATETIME" +
                            ")",

                    /* ==================== */
                    /*      ECONOMY         */
                    /* ==================== */
                    "CREATE TABLE IF NOT EXISTS player_economy (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "balance BIGINT DEFAULT 250, " +
                            "shards BIGINT DEFAULT 0" +
                            ")",

                    /* ==================== */
                    /*      ACTIVITY        */
                    /* ==================== */
                    "CREATE TABLE IF NOT EXISTS player_activity (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "playtime BIGINT DEFAULT 0, " +
                            "login_streak INT DEFAULT 0, " +
                            "longest_streak INT DEFAULT 0" +
                            ")",

                    /* ==================== */
                    /*        RANKS         */
                    /* ==================== */
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

                    /* ==================== */
                    /*       SETTINGS       */
                    /* ==================== */
                    "CREATE TABLE IF NOT EXISTS player_settings (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "pronouns VARCHAR(32) DEFAULT ''," +
                            "show_pronouns BOOLEAN DEFAULT TRUE," +
                            "selected_title VARCHAR(64) DEFAULT NULL" +
                            ")",

                    /* ==================== */
                    /*        TITLES        */
                    /* ==================== */
                    "CREATE TABLE IF NOT EXISTS player_titles (" +
                            "uuid VARCHAR(36) NOT NULL," +
                            "title VARCHAR(64) NOT NULL," +
                            "unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (uuid, title)" +
                            ")",

                    /* ==================== */
                    /*      RANK LOGS       */
                    /* ==================== */
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
                            ")"
            };

            for (String query : playerTables) {
                statement.addBatch(query);
            }

            int[] results = statement.executeBatch();
            connection.commit();

            Log.info("Database initialized successfully (" + results.length + " tables executed).");

        } catch (SQLException e) {
            Log.error("Database initialization failed: " + e.getMessage());
            throw e;
        } finally {
            try (Connection c = getConnection()) {
                c.setAutoCommit(true);
            }
        }
    }
}