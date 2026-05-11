package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Repository for activity-related persistence.
 * Handles playtime and streak operations.
 */
public class ActivityRepository {

    /**
     * Update playtime
     */
    public void updatePlaytime(String uuid, long newPlaytime) throws SQLException {
        String query = "UPDATE player_activity SET playtime = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, newPlaytime);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Add playtime to existing amount (atomic)
     */
    public void addPlaytime(String uuid, long amount) throws SQLException {
        String query = "UPDATE player_activity SET playtime = playtime + ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, amount);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Update login streak
     */
    public void updateLoginStreak(String uuid, int newStreak) throws SQLException {
        String query = "UPDATE player_activity SET login_streak = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, newStreak);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Update longest streak
     */
    public void updateLongestStreak(String uuid, int newStreak) throws SQLException {
        String query = "UPDATE player_activity SET longest_streak = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, newStreak);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }
}