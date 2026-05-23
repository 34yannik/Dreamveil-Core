package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.player.model.PlayerColor;
import de.yannik.dreamveilCore.player.model.PlayerSettings;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;

/**
 * Repository for {@code player_settings}.
 *
 * Schema (relevant columns):
 *   uuid, pronouns, show_pronouns, selected_title,
 *   chat_msg_color, chat_name_color        ← added by color system migration
 */
public class SettingsRepository {

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Load settings for a player. Returns null if no row exists yet.
     */
    public static PlayerSettings load(String uuid) {
        String sql = """
                SELECT pronouns, show_pronouns, selected_title,
                       chat_msg_color, chat_name_color
                FROM player_settings
                WHERE uuid = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                String   pronouns     = rs.getString("pronouns");
                boolean  showPronouns = rs.getBoolean("show_pronouns");
                Title    title        = parseTitle(rs.getString("selected_title"));
                PlayerColor msgColor  = PlayerColor.fromString(rs.getString("chat_msg_color"));
                PlayerColor nameColor = PlayerColor.fromString(rs.getString("chat_name_color"));

                return new PlayerSettings(uuid, pronouns, showPronouns, title, msgColor, nameColor);
            }

        } catch (SQLException e) {
            Log.error("SettingsRepository#load failed for " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    // ── WRITE ─────────────────────────────────────────────────────────────────

    /**
     * Insert a default settings row for a new player. Ignores duplicates.
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
     * Persist the full settings object (upsert).
     */
    public static void save(PlayerSettings settings) {
        String sql = """
                INSERT INTO player_settings
                    (uuid, pronouns, show_pronouns, selected_title, chat_msg_color, chat_name_color)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    pronouns       = VALUES(pronouns),
                    show_pronouns  = VALUES(show_pronouns),
                    selected_title = VALUES(selected_title),
                    chat_msg_color  = VALUES(chat_msg_color),
                    chat_name_color = VALUES(chat_name_color)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, settings.getUuid());
            stmt.setString(2, settings.getPronouns());
            stmt.setBoolean(3, settings.isShowPronouns());

            Title title = settings.getSelectedTitle();
            if (title != null) stmt.setString(4, title.name());
            else               stmt.setNull(4, Types.VARCHAR);

            setColorOrNull(stmt, 5, settings.getChatMsgColor());
            setColorOrNull(stmt, 6, settings.getChatNameColor());

            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#save failed for " + settings.getUuid() + ": " + e.getMessage());
        }
    }

    /** Update only the pronouns column. */
    public static void updatePronouns(String uuid, String pronouns) {
        updateStringColumn(uuid, "pronouns", pronouns);
    }

    /** Update only the show_pronouns column. */
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

    /** Update only the selected_title column. */
    public static void updateSelectedTitle(String uuid, Title title) {
        String sql = "UPDATE player_settings SET selected_title = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (title != null) stmt.setString(1, title.name());
            else               stmt.setNull(1, Types.VARCHAR);
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Log.error("SettingsRepository#updateSelectedTitle failed for " + uuid + ": " + e.getMessage());
        }
    }

    // ── INTERNAL ──────────────────────────────────────────────────────────────

    private static void updateStringColumn(String uuid, String column, String value) {
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

    /**
     * Sets a color as VARCHAR or NULL (DEFAULT is stored as NULL to keep the column lean).
     */
    private static void setColorOrNull(PreparedStatement stmt, int index, PlayerColor color)
            throws SQLException {
        if (color != null && color != PlayerColor.DEFAULT) stmt.setString(index, color.name());
        else                                               stmt.setNull(index, Types.VARCHAR);
    }

    private static Title parseTitle(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Title.valueOf(raw);
        } catch (IllegalArgumentException e) {
            Log.error("SettingsRepository: unknown title '" + raw + "' – ignored.");
            return null;
        }
    }
}