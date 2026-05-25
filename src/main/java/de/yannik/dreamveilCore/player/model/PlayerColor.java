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
    DEFAULT   ("Default",   "#FFFFFF", "#FFFFFF"),
    CHERRY    ("Cherry",    "#FF85A1", "#FF0054"),
    SKY       ("Sky",       "#90E0EF", "#0077B6"),

    // ────────── PLAYTIME ──────────
    OCEAN     ("Ocean",     "#00B4D8", "#023E8A"),   // 10 h
    FOREST    ("Forest",    "#2D6A4F", "#95D5B2"),   // 20 h
    ARCTIC    ("Arctic",    "#CAF0F8", "#48CAE4"),  // 30 h
    SUNSET    ("Sunset",    "#FF6B35", "#FF0080"),  // 50 h
    MIDNIGHT  ("Midnight",  "#3A0CA3", "#7209B7"),  // 100 h

    // ────────── PERMISSION ──────────
    GALAXY    ("Galaxy",    "#7B2FBE", "#4CC9F0"),
    LAVA      ("Lava",      "#E63946", "#FFBA08"),
    ROYAL     ("Royal",     "#FFD700", "#B8860B"),
    VOID      ("Void",      "#03071E", "#6A4C93");

    // ─────────────────────────────────────────────────────────────────────────

    private final String displayName;
    private final String startColor;        // hex e.g. "#FF6B35"
    private final String endColor;          // hex e.g. "#FF0080"

    PlayerColor(String displayName, String startColor, String endColor) {
        this.displayName        = displayName;
        this.startColor         = startColor;
        this.endColor           = endColor;
    }

    // ─────────────────────────────────────────────────────────────────────────

    public String getDisplayName()       { return displayName;        }
    public String getStartColor()        { return startColor;         }
    public String getEndColor()          { return endColor;           }

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