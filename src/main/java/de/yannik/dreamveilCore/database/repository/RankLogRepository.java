package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.rank.model.PlayerRank;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Repository for rank logging/history
 */
public class RankLogRepository {

    /**
     * Log a rank action (grant, revoke, expire)
     */
    public void logRankAction(String uuid, String playerName, PlayerRank rank, 
                            String action, LocalDateTime duration, String grantedBy) throws SQLException {
        String query = "INSERT INTO rank_logs (uuid, player_name, rank, action, duration_days, granted_by, logged_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            stmt.setString(3, rank.name());
            stmt.setString(4, action); // "GRANT", "REVOKE", "EXPIRE"
            
            // Calculate duration in days
            long durationDays = duration != null ? 
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), duration) : -1;
            stmt.setLong(5, durationDays);
            
            stmt.setString(6, grantedBy); // "ADMIN_USERNAME", "SYSTEM", etc
            stmt.executeUpdate();

            Log.info("Rank log: " + playerName + " | " + action + " | " + rank.name());
        }
    }

    /**
     * Get rank history for a player
     */
    public void getRankHistory(String uuid) throws SQLException {
        String query = "SELECT * FROM rank_logs WHERE uuid = ? ORDER BY logged_at DESC LIMIT 50";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String rankName = rs.getString("rank");
                    String action = rs.getString("action");
                    String grantedBy = rs.getString("granted_by");
                    LocalDateTime loggedAt = rs.getTimestamp("logged_at").toLocalDateTime();

                    Log.info("  -> " + action + " " + rankName + " by " + grantedBy + " at " + loggedAt);
                }
            }
        }
    }
}