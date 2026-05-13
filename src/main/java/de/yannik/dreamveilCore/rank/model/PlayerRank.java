package de.yannik.dreamveilCore.rank.model;

import de.yannik.dreamveilCore.rank.permission.Permission;
import org.bukkit.ChatColor;

public enum PlayerRank {

    PLAYER(
            "Player",
            0,
            ChatColor.GRAY,
            ChatColor.GRAY,
            new Permission[] {
            }
    ),

    VIP(
            "VIP",
            1,
            ChatColor.GRAY,
            ChatColor.GRAY,
            new Permission[] {
            }
    ),

    HELPER(
            "Helper",
            700,
            ChatColor.AQUA,
            ChatColor.BLUE,
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
            ChatColor.GOLD,
            ChatColor.RED,
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
            ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_PURPLE,
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
            ChatColor.DARK_RED,
            ChatColor.RED,
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