package de.yannik.dreamveilCore.rank.model;

import de.yannik.dreamveilCore.rank.permission.Permission;
import net.md_5.bungee.api.ChatColor;

public enum PlayerRank {

    PLAYER(
            "Player",
            0,
            ChatColor.of("#AAAAAA"), // Hellgrau
            ChatColor.of("#AAAAAA"), // Dunkleres Grau
            new Permission[] {
            }
    ),

    VIP(
            "VIP",
            1,
            ChatColor.of("#FFF56B"), // Goldgelb
            ChatColor.of("#FFB700"), // Orange-Gold
            new Permission[] {
            }
    ),

    HELPER(
            "Helper",
            700,
            ChatColor.of("#55FFFF"), // Cyan
            ChatColor.of("#0066FF"), // Blau
            new Permission[] {
                    // Team
                    Permission.TEAMLER_CHAT,
                    Permission.TEAMLER_DUTY,
                    Permission.TEAMLER_VANISH,
                    Permission.TEAMLER_STAFFLIST,
                    // Utility
                    Permission.TEAMLER_FLY,
                    Permission.TEAMLER_SPEED,
                    Permission.TEAMLER_GAMEMODE,
                    Permission.TEAMLER_BACK,
                    Permission.TEAMLER_TOP,
                    Permission.TEAMLER_BOTTOM,
                    Permission.TEAMLER_TP,
                    Permission.TEAMLER_TPHERE,
                    Permission.TEAMLER_TPPOS,
                    Permission.TEAMLER_INVSEE
            }
    ),

    MODERATOR(
            "Moderator",
            800,
            ChatColor.of("#FFAA00"), // Orange
            ChatColor.of("#FF3333"), // Rot
            new Permission[] {
                    // Team
                    Permission.TEAMLER_CHAT,
                    Permission.TEAMLER_DUTY,
                    Permission.TEAMLER_VANISH,
                    Permission.TEAMLER_STAFFLIST,
                    // Utility
                    Permission.TEAMLER_FLY,
                    Permission.TEAMLER_SPEED,
                    Permission.TEAMLER_GAMEMODE,
                    Permission.TEAMLER_BACK,
                    Permission.TEAMLER_TOP,
                    Permission.TEAMLER_BOTTOM,
                    Permission.TEAMLER_TP,
                    Permission.TEAMLER_TPHERE,
                    Permission.TEAMLER_TPPOS,
                    Permission.TEAMLER_TPALL,
                    Permission.TEAMLER_INVSEE,
                    // Moderation
                    Permission.TEAMLER_BROADCAST,
                    Permission.TEAMLER_CLEARCHAT,
                    Permission.TEAMLER_FREEZE,
                    // Ranks
                    Permission.RANK_GIVE,
                    Permission.RANK_REMOVE
            }
    ),

    DEVELOPER(
            "Developer",
            900,
            ChatColor.of("#FF55FF"), // Pink
            ChatColor.of("#8000FF"), // Lila
            new Permission[] {
                    // Team
                    Permission.TEAMLER_CHAT,
                    Permission.TEAMLER_DUTY,
                    Permission.TEAMLER_VANISH,
                    Permission.TEAMLER_STAFFLIST,
                    // Utility
                    Permission.TEAMLER_FLY,
                    Permission.TEAMLER_SPEED,
                    Permission.TEAMLER_GAMEMODE,
                    Permission.TEAMLER_BACK,
                    Permission.TEAMLER_TOP,
                    Permission.TEAMLER_BOTTOM,
                    Permission.TEAMLER_TP,
                    Permission.TEAMLER_TPHERE,
                    Permission.TEAMLER_TPPOS,
                    Permission.TEAMLER_TPALL,
                    Permission.TEAMLER_INVSEE,
                    // Moderation
                    Permission.TEAMLER_BROADCAST,
                    Permission.TEAMLER_CLEARCHAT,
                    Permission.TEAMLER_FREEZE,
                    // Admin/Server diagnostics
                    Permission.ADMIN_TPS,
                    Permission.ADMIN_UPTIME,
                    Permission.ADMIN_SERVERINFO,
                    Permission.ADMIN_CLEARINVENTORY
            }
    ),

    ADMIN(
            "Admin",
            1000,
            ChatColor.of("#FF9ED2"), // Pastel Pink
            ChatColor.of("#FF5FA2"), // Stärkeres Pink
            new Permission[] {
                    Permission.ALL_PERMISSIONS,
                    Permission.RANK_GIVE,
                    Permission.RANK_REMOVE,
                    Permission.RANK_TEAM_GIVE,
                    Permission.RANK_TEAM_REMOVE
            }
    );

    private final String displayName;
    private final int priority;
    private final ChatColor colorStart;
    private final ChatColor colorEnd;
    private final Permission[] permissions;

    PlayerRank(String displayName, int priority, ChatColor colorStart,
               ChatColor colorEnd, Permission[] permissions) {
        this.displayName = displayName;
        this.priority = priority;
        this.colorStart = colorStart;
        this.colorEnd = colorEnd;
        this.permissions = permissions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }

    public ChatColor getColorStart() {
        return colorStart;
    }

    public ChatColor getColorEnd() {
        return colorEnd;
    }

    /**
     * Get permissions as Permission enum array
     */
    public Permission[] getPermissionEnums() {
        return permissions;
    }

    /**
     * Get permissions as String array
     */
    public String[] getPermissions() {
        String[] result = new String[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            result[i] = permissions[i].getPermission();
        }
        return result;
    }

    public static PlayerRank fromString(String name) {
        for (PlayerRank rank : values()) {
            if (rank.name().equalsIgnoreCase(name) ||
                    rank.displayName.equalsIgnoreCase(name)) {
                return rank;
            }
        }
        return PLAYER;
    }

    public static PlayerRank getByPriority(int priority) {
        for (PlayerRank rank : values()) {
            if (rank.priority == priority) {
                return rank;
            }
        }
        return PLAYER;
    }
}