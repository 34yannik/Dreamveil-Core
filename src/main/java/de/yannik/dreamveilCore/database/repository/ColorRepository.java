package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.player.model.PlayerColor;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for the color system.
 *
 * Covers two tables:
 *   • {@code player_unlocked_colors}  – which colors a player owns
 *   • {@code player_settings}         – which colors are currently active
 *     (chat_msg_color / chat_name_color columns, written here to keep
 *      color logic co-located; full settings rows are handled by
 *      {@link SettingsRepository})
 */
public class ColorRepository {

    // ──────────────────────────────────────────────────────────────────────────
    // READ
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load all unlocked colors for a player from the database.
     * Returns an empty list (never null) on no rows or on error.
     */
    public List<PlayerColor> getUnlockedColors(String uuid) throws SQLException {
        String sql = "SELECT color_id FROM player_unlocked_colors WHERE uuid = ?";

        List<PlayerColor> colors = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String raw = rs.getString("color_id");
                    PlayerColor color = parseColor(raw);
                    if (color != null) colors.add(color);
                }
            }
        }
        return colors;
    }

    /**
     * Targeted single-color existence check.
     * Prefer using the cache whenever possible; this is the DB fallback.
     */
    public boolean isColorUnlocked(String uuid, PlayerColor color) throws SQLException {
        String sql = "SELECT 1 FROM player_unlocked_colors WHERE uuid = ? AND color_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, color.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WRITE – unlocked_colors
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Unlock a color for a player.
     * Uses INSERT IGNORE so duplicate calls are safe.
     */
    public void unlockColor(String uuid, PlayerColor color) throws SQLException {
        String sql = "INSERT IGNORE INTO player_unlocked_colors (uuid, color_id) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid);
            stmt.setString(2, color.name());
            stmt.executeUpdate();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WRITE – player_settings color columns
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Persist the active chat-message color in {@code player_settings}.
     * Pass {@code null} to reset to DEFAULT (stored as NULL in DB).
     */
    public void updateChatMsgColor(String uuid, PlayerColor color) throws SQLException {
        updateColorColumn(uuid, "chat_msg_color", color);
    }

    /**
     * Persist the active chat-name color in {@code player_settings}.
     * Pass {@code null} to reset to DEFAULT.
     */
    public void updateChatNameColor(String uuid, PlayerColor color) throws SQLException {
        updateColorColumn(uuid, "chat_name_color", color);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ──────────────────────────────────────────────────────────────────────────

    private void updateColorColumn(String uuid, String column, PlayerColor color) throws SQLException {
        String sql = "UPDATE player_settings SET " + column + " = ? WHERE uuid = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (color != null && color != PlayerColor.DEFAULT) {
                stmt.setString(1, color.name());
            } else {
                stmt.setNull(1, Types.VARCHAR); // NULL == DEFAULT
            }
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Safely parse a raw DB string to PlayerColor.
     * Logs and returns null for unknown values.
     */
    private PlayerColor parseColor(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return PlayerColor.valueOf(raw);
        } catch (IllegalArgumentException e) {
            Log.error("ColorRepository: unknown color '" + raw + "' in database – ignored.");
            return null;
        }
    }
}