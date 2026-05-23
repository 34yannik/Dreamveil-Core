package de.yannik.dreamveilCore.family.model;

/**
 * Hierarchy of roles within a Found Family.
 * Higher {@code level} = more authority.
 */
public enum FamilyRole {

    OWNER   ("Owner",    3),
    CO_OWNER("Co-Owner", 2),
    MEMBER  ("Member",   1);

    private final String displayName;
    private final int level;

    FamilyRole(String displayName, int level) {
        this.displayName = displayName;
        this.level       = level;
    }

    public String getDisplayName() { return displayName; }
    public int    getLevel()       { return level;       }

    /** True if this role has at least the authority of {@code other}. */
    public boolean isAtLeast(FamilyRole other) {
        return this.level >= other.level;
    }

    public static FamilyRole fromString(String raw) {
        if (raw == null) return MEMBER;
        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }
}