package de.yannik.dreamveilCore.player.model;

/**
 * Holds all settings for a single player.
 * Mirrors the player_settings table.
 */
public class PlayerSettings {

    private final String uuid;
    private String pronouns;
    private boolean showPronouns;
    private Title selectedTitle;

    public PlayerSettings(String uuid, String pronouns, boolean showPronouns, Title selectedTitle) {
        this.uuid          = uuid;
        this.pronouns      = pronouns;
        this.showPronouns  = showPronouns;
        this.selectedTitle = selectedTitle;
    }

    /** Default settings for a brand-new player */
    public static PlayerSettings defaultFor(String uuid) {
        return new PlayerSettings(uuid, "", true, null);
    }

    // ==================== GETTER ====================

    public String getUuid()           { return uuid;          }
    public String getPronouns()       { return pronouns;      }
    public boolean isShowPronouns()   { return showPronouns;  }
    public Title getSelectedTitle()   { return selectedTitle; }

    // ==================== SETTER ====================

    public void setPronouns(String pronouns)          { this.pronouns      = pronouns;      }
    public void setShowPronouns(boolean showPronouns) { this.showPronouns  = showPronouns;  }
    public void setSelectedTitle(Title title)         { this.selectedTitle = title;         }
}