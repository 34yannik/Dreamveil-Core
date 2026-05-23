package de.yannik.dreamveilCore.player.model;

/**
 * Defines all available chat colors as gradients.
 * Each color has a start/end hex value for gradient rendering.
 *
 * unlockRequirement format (nullable):
 *   null                  → free / always unlocked
 *   "playtime:X"          → X milliseconds of playtime required
 *   "permission:some.perm"→ permission node required
 */
public enum PlayerColor {

    // ────────── FREE ──────────
    DEFAULT   ("Default",   "#FFFFFF", "#FFFFFF", null),
    CHERRY    ("Cherry",    "#FF85A1", "#FF0054", null),
    SKY       ("Sky",       "#90E0EF", "#0077B6", null),

    // ────────── PLAYTIME ──────────
    OCEAN     ("Ocean",     "#00B4D8", "#023E8A", "playtime:36000000"),   // 10 h
    FOREST    ("Forest",    "#2D6A4F", "#95D5B2", "playtime:72000000"),   // 20 h
    ARCTIC    ("Arctic",    "#CAF0F8", "#48CAE4", "playtime:108000000"),  // 30 h
    SUNSET    ("Sunset",    "#FF6B35", "#FF0080", "playtime:180000000"),  // 50 h
    MIDNIGHT  ("Midnight",  "#3A0CA3", "#7209B7", "playtime:360000000"),  // 100 h

    // ────────── PERMISSION ──────────
    GALAXY    ("Galaxy",    "#7B2FBE", "#4CC9F0", "permission:dreamveil.color.galaxy"),
    LAVA      ("Lava",      "#E63946", "#FFBA08", "permission:dreamveil.color.lava"),
    ROYAL     ("Royal",     "#FFD700", "#B8860B", "permission:dreamveil.color.royal"),
    VOID      ("Void",      "#03071E", "#6A4C93", "permission:dreamveil.color.void");

    // ─────────────────────────────────────────────────────────────────────────

    private final String displayName;
    private final String startColor;        // hex e.g. "#FF6B35"
    private final String endColor;          // hex e.g. "#FF0080"
    private final String unlockRequirement; // nullable meta-string

    PlayerColor(String displayName, String startColor, String endColor, String unlockRequirement) {
        this.displayName        = displayName;
        this.startColor         = startColor;
        this.endColor           = endColor;
        this.unlockRequirement  = unlockRequirement;
    }

    // ─────────────────────────────────────────────────────────────────────────

    public String getDisplayName()       { return displayName;        }
    public String getStartColor()        { return startColor;         }
    public String getEndColor()          { return endColor;           }
    public String getUnlockRequirement() { return unlockRequirement;  }

    /** True when no requirement → every player starts with this color. */
    public boolean isFree() { return unlockRequirement == null; }

    /**
     * Returns the requirement type prefix, or null if none.
     * Examples: "playtime", "permission"
     */
    public String getRequirementType() {
        if (unlockRequirement == null) return null;
        int colon = unlockRequirement.indexOf(':');
        return colon >= 0 ? unlockRequirement.substring(0, colon) : null;
    }

    /**
     * Returns the requirement value (after the colon), or null if none.
     * Examples: "36000000", "dreamveil.color.galaxy"
     */
    public String getRequirementValue() {
        if (unlockRequirement == null) return null;
        int colon = unlockRequirement.indexOf(':');
        return colon >= 0 ? unlockRequirement.substring(colon + 1) : null;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Safe lookup – falls back to DEFAULT on unknown input.
     */
    public static PlayerColor fromString(String name) {
        if (name == null || name.isBlank()) return DEFAULT;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}