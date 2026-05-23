package de.yannik.dreamveilCore.family.model;

import java.time.LocalDateTime;

/**
 * Represents an active (or historical) buff instance for a family.
 * Mirrors the {@code family_buffs} table.
 */
public class FamilyActiveBuff {

    private final int           id;
    private final String        familyId;
    private final FamilyBuff    buffType;
    private final int           level;
    private final LocalDateTime expiresAt;    // null → permanent
    private final String        activatedBy;  // uuid
    private final LocalDateTime activatedAt;

    public FamilyActiveBuff(int id, String familyId, FamilyBuff buffType,
                            int level, LocalDateTime expiresAt,
                            String activatedBy, LocalDateTime activatedAt) {
        this.id          = id;
        this.familyId    = familyId;
        this.buffType    = buffType;
        this.level       = level;
        this.expiresAt   = expiresAt;
        this.activatedBy = activatedBy;
        this.activatedAt = activatedAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int           getId()           { return id;          }
    public String        getFamilyId()     { return familyId;    }
    public FamilyBuff    getBuffType()     { return buffType;    }
    public int           getLevel()        { return level;       }
    public LocalDateTime getExpiresAt()    { return expiresAt;   }
    public String        getActivatedBy()  { return activatedBy; }
    public LocalDateTime getActivatedAt()  { return activatedAt; }

    /** True if the buff has expired (or has an expiry and it's in the past). */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /** True if the buff is currently active. */
    public boolean isActive() {
        return !isExpired();
    }
}