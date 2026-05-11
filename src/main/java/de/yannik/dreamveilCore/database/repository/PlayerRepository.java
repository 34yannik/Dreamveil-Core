package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.player.model.PlayerData;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;
import java.time.LocalDateTime;

public class PlayerRepository {

    /**
     * Load complete player data from database
     */
    public PlayerData loadPlayer(String uuid) throws SQLException {
        String query = "SELECT * FROM player_core c " +
                "LEFT JOIN player_economy e ON c.uuid = e.uuid " +
                "LEFT JOIN player_activity a ON c.uuid = a.uuid " +
                "WHERE c.uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlayerData(rs);
                }
            }
        }

        Log.info("Player not found in database: " + uuid);
        return null;
    }

    /**
     * Check if player exists in database
     */
    public boolean playerExists(String uuid) throws SQLException {
        String query = "SELECT 1 FROM player_core WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Create new player record
     */
    public void createPlayer(String uuid, String username) throws SQLException {
        String query = "INSERT INTO player_core (uuid, username, first_login, last_login, last_logout) " +
                "VALUES (?, ?, NOW(), NOW(), NULL)";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            stmt.setString(2, username);
            stmt.executeUpdate();

        }

        // Also create economy and activity records
        String economyQuery = "INSERT INTO player_economy (uuid, balance, shards) VALUES (?, 250, 0)";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(economyQuery)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        }

        String activityQuery = "INSERT INTO player_activity (uuid, playtime, login_streak, longest_streak) " +
                "VALUES (?, 0, 0, 0)";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(activityQuery)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Update player core data
     */
    public void updatePlayerCore(PlayerData player) throws SQLException {
        String query = "UPDATE player_core SET username = ?, last_login = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, player.getUsername());
            stmt.setTimestamp(2, Timestamp.valueOf(player.getLastLogin()));
            stmt.setString(3, player.getUuid());

            stmt.executeUpdate();
        }
    }

    /**
     * Update last logout timestamp
     */
    public void updateLastLogout(String uuid) throws SQLException {
        String query = "UPDATE player_core SET last_logout = NOW() WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Map ResultSet to PlayerData object
     */
    private PlayerData mapResultSetToPlayerData(ResultSet rs) throws SQLException {
        String uuid = rs.getString("uuid");
        String username = rs.getString("username");
        LocalDateTime firstLogin = rs.getTimestamp("first_login") != null ?
                rs.getTimestamp("first_login").toLocalDateTime() : null;
        LocalDateTime lastLogin = rs.getTimestamp("last_login") != null ?
                rs.getTimestamp("last_login").toLocalDateTime() : null;
        LocalDateTime lastLogout = rs.getTimestamp("last_logout") != null ?
                rs.getTimestamp("last_logout").toLocalDateTime() : null;

        long balance = rs.getLong("balance");
        long shards = rs.getLong("shards");
        long playtime = rs.getLong("playtime");
        int loginStreak = rs.getInt("login_streak");
        int longestStreak = rs.getInt("longest_streak");

        return new PlayerData(uuid, username, firstLogin, lastLogin, lastLogout,
                balance, shards, playtime, loginStreak, longestStreak);
    }
}