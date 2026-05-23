package de.yannik.dreamveilCore.family.model;

/**
 * Activatable buffs that a family can apply.
 * Each buff has a display name, description, a base duration in hours,
 * and a maximum level (stackable by upgrading).
 */
public enum FamilyBuff {

    XP_BOOST        ("XP Boost",          "Increases XP gain for all members",          24, 3),
    COIN_BOOST       ("Coin Boost",         "Increases coin earnings for all members",     24, 3),
    PLAYTIME_BOOST   ("Playtime Boost",     "Doubles tracked playtime for streak purposes",12, 2),
    STREAK_PROTECTION("Streak Protection",  "Protects each member's login streak once",    48, 1),
    DROP_BOOST       ("Drop Boost",         "Increases item drop rates for all members",   12, 2);

    private final String displayName;
    private final String description;
    private final int    baseDurationHours;  // duration per activation at level 1
    private final int    maxLevel;

    FamilyBuff(String displayName, String description, int baseDurationHours, int maxLevel) {
        this.displayName       = displayName;
        this.description       = description;
        this.baseDurationHours = baseDurationHours;
        this.maxLevel          = maxLevel;
    }

    public String getDisplayName()     { return displayName;       }
    public String getDescription()     { return description;       }
    public int    getBaseDurationHours(){ return baseDurationHours; }
    public int    getMaxLevel()        { return maxLevel;          }

    /**
     * Effective duration for the given level.
     * Each level adds 50 % of the base duration.
     */
    public int getEffectiveDurationHours(int level) {
        int clampedLevel = Math.max(1, Math.min(level, maxLevel));
        return (int) (baseDurationHours * (1 + 0.5 * (clampedLevel - 1)));
    }

    public static FamilyBuff fromString(String raw) {
        if (raw == null) return null;
        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}