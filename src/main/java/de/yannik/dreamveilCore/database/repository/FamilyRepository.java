package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.family.model.*;
import de.yannik.dreamveilCore.util.Log;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository for the Found Family system.
 *
 * Tables managed:
 *   families, family_members, family_home, family_buffs
 */
public class FamilyRepository {

    // ──────────────────────────────────────────────────────────────────────────
    // FAMILIES
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load a family by its ID, including the current member count.
     * Returns null if not found.
     */
    public Family getFamilyById(String familyId) throws SQLException {
        String sql = """
                SELECT f.id, f.name, f.owner_uuid, f.description, f.created_at,
                       COUNT(m.player_uuid) AS member_count
                FROM families f
                LEFT JOIN family_members m ON m.family_id = f.id
                WHERE f.id = ?
                GROUP BY f.id
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, familyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapFamily(rs);
            }
        }
        return null;
    }

    /**
     * Load the family a player belongs to, or null if they have none.
     */
    public Family getFamilyByPlayer(String playerUuid) throws SQLException {
        String sql = """
                SELECT f.id, f.name, f.owner_uuid, f.description, f.created_at,
                       COUNT(m2.player_uuid) AS member_count
                FROM family_members m
                JOIN families f ON f.id = m.family_id
                LEFT JOIN family_members m2 ON m2.family_id = f.id
                WHERE m.player_uuid = ?
                GROUP BY f.id
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapFamily(rs);
            }
        }
        return null;
    }

    /**
     * Create a new family. The owner is automatically added as a member with OWNER role.
     *
     * @return The ID of the newly created family.
     */
    public String createFamily(String ownerUuid, String ownerName,
                               String name, String description) throws SQLException {
        String familyId = UUID.randomUUID().toString();

        String familySql = """
                INSERT INTO families (id, name, owner_uuid, description, created_at)
                VALUES (?, ?, ?, ?, NOW())
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(familySql)) {

            stmt.setString(1, familyId);
            stmt.setString(2, name);
            stmt.setString(3, ownerUuid);
            stmt.setString(4, description);
            stmt.executeUpdate();
        }

        // Add owner as OWNER member
        addMember(familyId, ownerUuid, ownerName, FamilyRole.OWNER);

        Log.info("FamilyRepository: created family '" + name + "' (id=" + familyId + ") by " + ownerUuid);
        return familyId;
    }

    /**
     * Delete a family and cascade-delete its members, home, and buffs.
     */
    public void deleteFamily(String familyId) throws SQLException {
        // Cascade via DB foreign keys or explicit deletes
        deleteAllMembers(familyId);
        deleteHome(familyId);
        deleteAllBuffs(familyId);

        String sql = "DELETE FROM families WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.executeUpdate();
        }
        Log.info("FamilyRepository: deleted family " + familyId);
    }

    /**
     * Update name and/or description.
     */
    public void updateFamily(String familyId, String newName, String newDescription) throws SQLException {
        String sql = "UPDATE families SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, newDescription);
            stmt.setString(3, familyId);
            stmt.executeUpdate();
        }
    }

    /**
     * Transfer ownership to another member (must already be a member).
     * Does NOT change the old owner's role – caller is responsible.
     */
    public void transferOwnership(String familyId, String newOwnerUuid) throws SQLException {
        String sql = "UPDATE families SET owner_uuid = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newOwnerUuid);
            stmt.setString(2, familyId);
            stmt.executeUpdate();
        }
    }

    /**
     * True if a family with this name already exists (case-insensitive).
     */
    public boolean nameExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM families WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MEMBERS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load all members of a family.
     */
    public List<FamilyMember> getMembers(String familyId) throws SQLException {
        String sql = """
                SELECT family_id, player_uuid, player_name, role, joined_at
                FROM family_members
                WHERE family_id = ?
                ORDER BY
                    CASE role
                        WHEN 'OWNER'    THEN 1
                        WHEN 'CO_OWNER' THEN 2
                        ELSE 3
                    END,
                    joined_at ASC
                """;

        List<FamilyMember> members = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, familyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) members.add(mapMember(rs));
            }
        }
        return members;
    }

    /**
     * Load the membership record for a specific player, or null if not a member.
     */
    public FamilyMember getMember(String familyId, String playerUuid) throws SQLException {
        String sql = "SELECT * FROM family_members WHERE family_id = ? AND player_uuid = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapMember(rs);
            }
        }
        return null;
    }

    /** True if the player is already a member of any family. */
    public boolean isInAnyFamily(String playerUuid) throws SQLException {
        String sql = "SELECT 1 FROM family_members WHERE player_uuid = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Add a player to a family.
     */
    public void addMember(String familyId, String playerUuid,
                          String playerName, FamilyRole role) throws SQLException {
        String sql = """
                INSERT INTO family_members (family_id, player_uuid, player_name, role, joined_at)
                VALUES (?, ?, ?, ?, NOW())
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid);
            stmt.setString(3, playerName);
            stmt.setString(4, role.name());
            stmt.executeUpdate();
        }
    }

    /**
     * Remove a player from a family.
     */
    public void removeMember(String familyId, String playerUuid) throws SQLException {
        String sql = "DELETE FROM family_members WHERE family_id = ? AND player_uuid = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, playerUuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Update a member's role.
     */
    public void updateMemberRole(String familyId, String playerUuid, FamilyRole role) throws SQLException {
        String sql = "UPDATE family_members SET role = ? WHERE family_id = ? AND player_uuid = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            stmt.setString(2, familyId);
            stmt.setString(3, playerUuid);
            stmt.executeUpdate();
        }
    }

    /** Remove all members (used on family deletion). */
    private void deleteAllMembers(String familyId) throws SQLException {
        String sql = "DELETE FROM family_members WHERE family_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.executeUpdate();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HOME
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load the family home, or null if none has been set.
     */
    public FamilyHome getHome(String familyId) throws SQLException {
        String sql = "SELECT * FROM family_home WHERE family_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, familyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapHome(rs);
            }
        }
        return null;
    }

    /**
     * Set (or replace) the family home location.
     */
    public void setHome(String familyId, String world,
                        double x, double y, double z,
                        float yaw, float pitch,
                        String setByUuid) throws SQLException {
        String sql = """
                INSERT INTO family_home (family_id, world, x, y, z, yaw, pitch, set_by, set_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE
                    world  = VALUES(world),
                    x      = VALUES(x),
                    y      = VALUES(y),
                    z      = VALUES(z),
                    yaw    = VALUES(yaw),
                    pitch  = VALUES(pitch),
                    set_by = VALUES(set_by),
                    set_at = NOW()
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, world);
            stmt.setDouble(3, x);
            stmt.setDouble(4, y);
            stmt.setDouble(5, z);
            stmt.setFloat(6, yaw);
            stmt.setFloat(7, pitch);
            stmt.setString(8, setByUuid);
            stmt.executeUpdate();
        }
    }

    /** Delete the family home. */
    private void deleteHome(String familyId) throws SQLException {
        String sql = "DELETE FROM family_home WHERE family_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.executeUpdate();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BUFFS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load all currently active (non-expired) buffs for a family.
     */
    public List<FamilyActiveBuff> getActiveBuffs(String familyId) throws SQLException {
        String sql = """
                SELECT * FROM family_buffs
                WHERE family_id = ?
                  AND (expires_at IS NULL OR expires_at > NOW())
                ORDER BY activated_at DESC
                """;

        List<FamilyActiveBuff> buffs = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, familyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) buffs.add(mapBuff(rs));
            }
        }
        return buffs;
    }

    /**
     * Activate a buff for a family.
     * If the same buff_type is already active, it is replaced (upsert by family_id + buff_type).
     */
    public void activateBuff(String familyId, FamilyBuff buffType,
                             int level, LocalDateTime expiresAt,
                             String activatedByUuid) throws SQLException {
        // Delete existing same-type buff first so we can re-insert cleanly
        String deleteSql = "DELETE FROM family_buffs WHERE family_id = ? AND buff_type = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, buffType.name());
            stmt.executeUpdate();
        }

        String sql = """
                INSERT INTO family_buffs
                    (family_id, buff_type, level, expires_at, activated_by, activated_at)
                VALUES (?, ?, ?, ?, ?, NOW())
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.setString(2, buffType.name());
            stmt.setInt(3, level);
            if (expiresAt != null) stmt.setTimestamp(4, Timestamp.valueOf(expiresAt));
            else                   stmt.setNull(4, Types.TIMESTAMP);
            stmt.setString(5, activatedByUuid);
            stmt.executeUpdate();
        }
        Log.info("FamilyRepository: activated buff " + buffType.name()
                + " lvl " + level + " for family " + familyId);
    }

    /** Remove all buffs (used on family deletion). */
    private void deleteAllBuffs(String familyId) throws SQLException {
        String sql = "DELETE FROM family_buffs WHERE family_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, familyId);
            stmt.executeUpdate();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MAPPERS
    // ──────────────────────────────────────────────────────────────────────────

    private Family mapFamily(ResultSet rs) throws SQLException {
        return new Family(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("owner_uuid"),
                rs.getString("description"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getInt("member_count")
        );
    }

    private FamilyMember mapMember(ResultSet rs) throws SQLException {
        return new FamilyMember(
                rs.getString("family_id"),
                rs.getString("player_uuid"),
                rs.getString("player_name"),
                FamilyRole.fromString(rs.getString("role")),
                rs.getTimestamp("joined_at") != null
                        ? rs.getTimestamp("joined_at").toLocalDateTime() : null
        );
    }

    private FamilyHome mapHome(ResultSet rs) throws SQLException {
        return new FamilyHome(
                rs.getString("family_id"),
                rs.getString("world"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getFloat("yaw"),
                rs.getFloat("pitch"),
                rs.getString("set_by"),
                rs.getTimestamp("set_at") != null
                        ? rs.getTimestamp("set_at").toLocalDateTime() : null
        );
    }

    private FamilyActiveBuff mapBuff(ResultSet rs) throws SQLException {
        String   buffRaw  = rs.getString("buff_type");
        FamilyBuff buff   = FamilyBuff.fromString(buffRaw);
        Timestamp expires = rs.getTimestamp("expires_at");

        return new FamilyActiveBuff(
                rs.getInt("id"),
                rs.getString("family_id"),
                buff,
                rs.getInt("level"),
                expires != null ? expires.toLocalDateTime() : null,
                rs.getString("activated_by"),
                rs.getTimestamp("activated_at").toLocalDateTime()
        );
    }
}