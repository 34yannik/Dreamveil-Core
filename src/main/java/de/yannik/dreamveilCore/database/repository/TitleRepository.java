package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TitleRepository {

    // ==================== READ ====================

    /**
     * Returns all titles the player has unlocked.
     */
    public static List<Title> loadUnlocked(String uuid) {
        String sql = "SELECT title FROM player_titles WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);

            List<Title> titles = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String raw = rs.getString("title");
                    try {
                        titles.add(Title.valueOf(raw));
                    } catch (IllegalArgumentException e) {
                        Log.error("TitleRepository: unknown title '" + raw + "' in database – ignored.");
                    }
                }
            }
            return titles;

        } catch (SQLException e) {
            Log.error("TitleRepository#loadUnlocked failed for " + uuid + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Check whether a player has a specific title unlocked.
     */
    public static boolean hasTitle(String uuid, Title title) {
        String sql = "SELECT 1 FROM player_titles WHERE uuid = ? AND title = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, title.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            Log.error("TitleRepository#hasTitle failed for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    // ==================== WRITE ====================

    /**
     * Unlock a title for a player. Ignores duplicates (already unlocked).
     */
    public static void unlock(String uuid, Title title) {
        String sql = "INSERT IGNORE INTO player_titles (uuid, title) VALUES (?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, title.name());
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("TitleRepository#unlock failed for " + uuid + ", title=" + title.name() + ": " + e.getMessage());
        }
    }

    /**
     * Revoke a title from a player.
     */
    public static void revoke(String uuid, Title title) {
        String sql = "DELETE FROM player_titles WHERE uuid = ? AND title = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, title.name());
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("TitleRepository#revoke failed for " + uuid + ", title=" + title.name() + ": " + e.getMessage());
        }
    }

    /**
     * Revoke all titles from a player (e.g. on reset).
     */
    public static void revokeAll(String uuid) {
        String sql = "DELETE FROM player_titles WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("TitleRepository#revokeAll failed for " + uuid + ": " + e.getMessage());
        }
    }
}