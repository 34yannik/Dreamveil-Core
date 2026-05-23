package de.yannik.dreamveilCore.player.model;

/**
 * Holds all settings for a single player.
 * Mirrors the player_settings table.
 *
 * Extended: chat_msg_color, chat_name_color
 */
public class PlayerSettings {

    private final String uuid;
    private String pronouns;
    private boolean showPronouns;
    private Title selectedTitle;

    // ── Color System ──────────────────────────────────────────────────────────
    private PlayerColor chatMsgColor;   // active gradient for chat messages
    private PlayerColor chatNameColor;  // active gradient for player name in chat

    // ─────────────────────────────────────────────────────────────────────────

    public PlayerSettings(String uuid,
                          String pronouns,
                          boolean showPronouns,
                          Title selectedTitle,
                          PlayerColor chatMsgColor,
                          PlayerColor chatNameColor) {
        this.uuid          = uuid;
        this.pronouns      = pronouns;
        this.showPronouns  = showPronouns;
        this.selectedTitle = selectedTitle;
        this.chatMsgColor  = chatMsgColor  != null ? chatMsgColor  : PlayerColor.DEFAULT;
        this.chatNameColor = chatNameColor != null ? chatNameColor : PlayerColor.DEFAULT;
    }

    /** Default settings for a brand-new player. */
    public static PlayerSettings defaultFor(String uuid) {
        return new PlayerSettings(uuid, "", true, null, PlayerColor.DEFAULT, PlayerColor.DEFAULT);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String      getUuid()          { return uuid;          }
    public String      getPronouns()      { return pronouns;      }
    public boolean     isShowPronouns()   { return showPronouns;  }
    public Title       getSelectedTitle() { return selectedTitle; }
    public PlayerColor getChatMsgColor()  { return chatMsgColor;  }
    public PlayerColor getChatNameColor() { return chatNameColor; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setPronouns(String pronouns)            { this.pronouns      = pronouns;      }
    public void setShowPronouns(boolean showPronouns)   { this.showPronouns  = showPronouns;  }
    public void setSelectedTitle(Title title)           { this.selectedTitle = title;         }
    public void setChatMsgColor(PlayerColor color)      { this.chatMsgColor  = color  != null ? color  : PlayerColor.DEFAULT; }
    public void setChatNameColor(PlayerColor color)     { this.chatNameColor = color  != null ? color  : PlayerColor.DEFAULT; }
}