package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.player.model.PlayerSettings;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;

public class SettingsRepository {

    // ==================== READ ====================

    /**
     * Load settings for a player. Returns null if no row exists yet.
     */
    public static PlayerSettings load(String uuid) {
        String sql = "SELECT pronouns, show_pronouns, selected_title FROM player_settings WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                String pronouns     = rs.getString("pronouns");
                boolean showPronouns = rs.getBoolean("show_pronouns");
                String rawTitle     = rs.getString("selected_title");
                Title selectedTitle = parseTitle(rawTitle);

                return new PlayerSettings(uuid, pronouns, showPronouns, selectedTitle);
            }

        } catch (SQLException e) {
            Log.error("SettingsRepository#load failed for " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    // ==================== WRITE ====================

    /**
     * Insert default settings row for a new player. Ignores duplicates.
     */
    public static void insertDefault(String uuid) {
        String sql = "INSERT IGNORE INTO player_settings (uuid) VALUES (?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#insertDefault failed for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Persist the full settings object back to the database.
     */
    public static void save(PlayerSettings settings) {
        String sql = """
                INSERT INTO player_settings (uuid, pronouns, show_pronouns, selected_title)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    pronouns       = VALUES(pronouns),
                    show_pronouns  = VALUES(show_pronouns),
                    selected_title = VALUES(selected_title)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, settings.getUuid());
            stmt.setString(2, settings.getPronouns());
            stmt.setBoolean(3, settings.isShowPronouns());

            Title title = settings.getSelectedTitle();
            if (title != null) {
                stmt.setString(4, title.name());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#save failed for " + settings.getUuid() + ": " + e.getMessage());
        }
    }

    /** Update only the pronouns column */
    public static void updatePronouns(String uuid, String pronouns) {
        updateColumn(uuid, "pronouns", pronouns);
    }

    /** Update only the show_pronouns column */
    public static void updateShowPronouns(String uuid, boolean showPronouns) {
        String sql = "UPDATE player_settings SET show_pronouns = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setBoolean(1, showPronouns);
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#updateShowPronouns failed for " + uuid + ": " + e.getMessage());
        }
    }

    /** Update only the selected_title column */
    public static void updateSelectedTitle(String uuid, Title title) {
        String sql = "UPDATE player_settings SET selected_title = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (title != null) {
                stmt.setString(1, title.name());
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#updateSelectedTitle failed for " + uuid + ": " + e.getMessage());
        }
    }

    // ==================== INTERNAL ====================

    private static void updateColumn(String uuid, String column, String value) {
        String sql = "UPDATE player_settings SET " + column + " = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#updateColumn(" + column + ") failed for " + uuid + ": " + e.getMessage());
        }
    }

    private static Title parseTitle(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Title.valueOf(raw);
        } catch (IllegalArgumentException e) {
            Log.error("SettingsRepository: unknown title '" + raw + "' in database – ignored.");
            return null;
        }
    }
}