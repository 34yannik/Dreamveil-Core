package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.rank.model.PlayerRank;
import de.yannik.dreamveilCore.rank.model.PlayerRankData;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for rank operations
 */
public class RankRepository {

    /**
     * Get all active ranks for a player (not expired)
     */
    public List<PlayerRankData> getPlayerRanks(String uuid) throws SQLException {
        String query = "SELECT * FROM player_ranks WHERE uuid = ? AND (expires_at IS NULL OR expires_at > NOW())";

        List<PlayerRankData> ranks = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ranks.add(mapResultSetToRankData(rs, uuid));
                }
            }
        }

        return ranks;
    }

    /**
     * Get expired ranks for a player (for cleanup)
     */
    public List<PlayerRankData> getExpiredRanks(String uuid) throws SQLException {
        String query = "SELECT * FROM player_ranks WHERE uuid = ? AND expires_at IS NOT NULL AND expires_at <= NOW()";

        List<PlayerRankData> ranks = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ranks.add(mapResultSetToRankData(rs, uuid));
                }
            }
        }

        return ranks;
    }

    /**
     * Add rank to player
     */
    public void addRank(String uuid, PlayerRank rank, LocalDateTime expiresAt, String grantedBy) throws SQLException {
        String query = "INSERT INTO player_ranks (uuid, player_rank, created_at, expires_at, granted_by) " +
                "VALUES (?, ?, NOW(), ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            stmt.setString(2, rank.name());
            stmt.setTimestamp(3, expiresAt != null ? Timestamp.valueOf(expiresAt) : null);
            stmt.setString(4, grantedBy);

            stmt.executeUpdate();
            Log.info("Rank added: " + uuid + " -> " + rank.name());
        }
    }

    /**
     * Remove rank from player
     */
    public void removeRank(String uuid, PlayerRank rank) throws SQLException {
        String query = "DELETE FROM player_ranks WHERE uuid = ? AND player_rank = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            stmt.setString(2, rank.name());

            stmt.executeUpdate();
            Log.info("Rank removed: " + uuid + " <- " + rank.name());
        }
    }

    /**
     * Remove all expired ranks for a player
     */
    public List<PlayerRankData> removeExpiredRanks(String uuid) throws SQLException {
        List<PlayerRankData> expired = getExpiredRanks(uuid);

        String query = "DELETE FROM player_ranks WHERE uuid = ? AND expires_at IS NOT NULL AND expires_at <= NOW()";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, uuid);
            stmt.executeUpdate();

            Log.info("Removed " + expired.size() + " expired ranks for " + uuid);
        }

        return expired;
    }

    /**
     * Map ResultSet to PlayerRankData
     */
    private PlayerRankData mapResultSetToRankData(ResultSet rs, String uuid) throws SQLException {
        String rankStr = rs.getString("player_rank");
        PlayerRank rank = PlayerRank.fromString(rankStr);
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        LocalDateTime expiresAt = rs.getTimestamp("expires_at") != null ?
                rs.getTimestamp("expires_at").toLocalDateTime() : null;
        String grantedBy = rs.getString("granted_by");

        return new PlayerRankData(uuid, rank, createdAt, expiresAt, grantedBy);
    }
}