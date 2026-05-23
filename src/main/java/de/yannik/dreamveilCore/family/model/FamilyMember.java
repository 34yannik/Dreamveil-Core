package de.yannik.dreamveilCore.family.model;

import java.time.LocalDateTime;

/**
 * Represents a player's membership in a Found Family.
 * Mirrors the {@code family_members} table.
 */
public class FamilyMember {

    private final String        familyId;
    private final String        playerUuid;
    private       String        playerName;  // denormalized for display
    private       FamilyRole    role;
    private final LocalDateTime joinedAt;

    public FamilyMember(String familyId, String playerUuid,
                        String playerName, FamilyRole role, LocalDateTime joinedAt) {
        this.familyId   = familyId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.role       = role;
        this.joinedAt   = joinedAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String        getFamilyId()   { return familyId;   }
    public String        getPlayerUuid() { return playerUuid; }
    public String        getPlayerName() { return playerName; }
    public FamilyRole    getRole()       { return role;       }
    public LocalDateTime getJoinedAt()  { return joinedAt;   }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setPlayerName(String name) { this.playerName = name; }
    public void setRole(FamilyRole role)   { this.role       = role; }
}