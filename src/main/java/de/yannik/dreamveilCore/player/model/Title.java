package de.yannik.dreamveilCore.player.model;

import de.yannik.dreamveilCore.util.GradientUtil;
import net.md_5.bungee.api.ChatColor;

/**
 * All available titles in DreamveilCore.
 *
 * Adding a new title:
 *   MY_TITLE("My Title", ChatColor.of("#RRGGBB"), ChatColor.of("#RRGGBB"), false)
 *
 * Bold titles apply the gradient per-character with a BOLD prefix injected
 * between each color code, so the gradient stays visible in bold rendering.
 */
public enum Title {

    /* ==================== */
    /*       LGBTQIA+       */
    /* ==================== */
    FEMBOY     ("Femboy",      ChatColor.of("#FF8DC7"), ChatColor.of("#A0C4FF"), true),
    ENBY       ("Enby",        ChatColor.of("#FFF700"), ChatColor.of("#FFFFFF"), false),
    PRIDE      ("Pride",       ChatColor.of("#FF0000"), ChatColor.of("#8B00FF"), true),
    BLOSSOM    ("Blossom",     ChatColor.of("#FFB7D5"), ChatColor.of("#FF69B4"), false),
    NEBULA     ("Nebula",      ChatColor.of("#7F00FF"), ChatColor.of("#E100FF"), true),
    AURORA     ("Aurora",      ChatColor.of("#00F5A0"), ChatColor.of("#00D9F5"), false),
    VELVET     ("Velvet",      ChatColor.of("#FF4E9B"), ChatColor.of("#C850C0"), true),
    STARLIGHT  ("Starlight",   ChatColor.of("#F8FFAE"), ChatColor.of("#43C6AC"), false),
    FLARE      ("Flare",       ChatColor.of("#FF512F"), ChatColor.of("#DD2476"), true),
    CHROMA     ("Chroma",      ChatColor.of("#00C9FF"), ChatColor.of("#92FE9D"), false),
    SAPPHIC    ("Sapphic",     ChatColor.of("#FF8BA7"), ChatColor.of("#C3AED6"), false),
    ACHILLEAN  ("Achillean",   ChatColor.of("#4FACFE"), ChatColor.of("#00F2FE"), false),
    TRANSCEND  ("Transcend",   ChatColor.of("#5BCEFA"), ChatColor.of("#F5A9B8"), true),
    PANSPARK   ("Panspark",    ChatColor.of("#FF1B8D"), ChatColor.of("#FFD800"), true),
    ACE        ("Ace",         ChatColor.of("#2E2E2E"), ChatColor.of("#A259FF"), false),
    NOVA       ("Nova",        ChatColor.of("#FF6FD8"), ChatColor.of("#3813C2"), true);

    // ================================================================

    private final String displayName;
    private final ChatColor startColor;
    private final ChatColor endColor;
    private final boolean bold;

    Title(String displayName, ChatColor startColor, ChatColor endColor, boolean bold) {
        this.displayName = displayName;
        this.startColor  = startColor;
        this.endColor    = endColor;
        this.bold        = bold;
    }

    // ==================== GETTER ====================

    public String getDisplayName() { return displayName; }
    public ChatColor getStartColor() { return startColor; }
    public ChatColor getEndColor()   { return endColor;   }
    public boolean isBold()          { return bold;        }

    /**
     * Returns the formatted title string ready for chat/display.
     * Bold titles inject ChatColor.BOLD after every per-character color code
     * so the gradient remains fully visible even in bold rendering.
     */
    public String getFormatted() {
        return bold ? applyBoldGradient() : GradientUtil.applyGradient(displayName, startColor, endColor);
    }

    // ==================== INTERNAL ====================

    private String applyBoldGradient() {
        java.awt.Color start = startColor.getColor();
        java.awt.Color end   = endColor.getColor();

        // Fallback – no AWT color available (e.g. legacy named color)
        if (start == null || end == null) {
            return startColor + "" + ChatColor.BOLD + displayName + ChatColor.RESET;
        }

        StringBuilder result = new StringBuilder();
        int length = displayName.length();

        for (int i = 0; i < length; i++) {
            double progress = (length == 1) ? 0 : (double) i / (length - 1);

            int r = interpolate(start.getRed(),   end.getRed(),   progress);
            int g = interpolate(start.getGreen(), end.getGreen(), progress);
            int b = interpolate(start.getBlue(),  end.getBlue(),  progress);

            ChatColor color = ChatColor.of(new java.awt.Color(r, g, b));

            // color code → bold → character  (bold must come after the color reset)
            result.append(color)
                  .append(ChatColor.BOLD)
                  .append(displayName.charAt(i));
        }

        result.append(ChatColor.RESET);
        return result.toString();
    }

    private static int interpolate(int from, int to, double progress) {
        return (int) (from + (to - from) * progress);
    }
}