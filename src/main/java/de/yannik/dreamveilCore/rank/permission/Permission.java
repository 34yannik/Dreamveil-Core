package de.yannik.dreamveilCore.rank.permission;

/**
 * All permissions used in DreamveilCore and related plugins
 * Type-safe permission management
 */
public enum Permission {

    // ==================== TEAMLER ====================
    TEAMLER_CHAT("dreamveil.teamler.chat", "Can use teamchat"),
    TEAMLER_DUTY("dreamveil.teamler.duty", "Join team duty"),
    TEAMLER_VANISH("dreamveil.teamler.vanish", "Can use vanish"),
    TEAMLER_FLY("dreamveil.teamler.fly", "Can toggle fly mode"),
    TEAMLER_SPEED("dreamveil.teamler.speed", "Can change movement speed"),
    TEAMLER_GAMEMODE("dreamveil.teamler.gamemode", "Can change gamemode"),
    TEAMLER_BACK("dreamveil.teamler.back", "Can teleport back to last location"),
    TEAMLER_INVSEE("dreamveil.teamler.invsee", "Can view player inventories"),
    TEAMLER_TP("dreamveil.teamler.tp", "Can teleport to players"),
    TEAMLER_TPHERE("dreamveil.teamler.tphere", "Can teleport players to self"),
    TEAMLER_TPALL("dreamveil.teamler.tpall", "Can teleport all players to self"),
    TEAMLER_TPPOS("dreamveil.teamler.tppos", "Can teleport to coordinates"),
    TEAMLER_TOP("dreamveil.teamler.top", "Can teleport to highest block"),
    TEAMLER_BOTTOM("dreamveil.teamler.bottom", "Can teleport to lowest safe block"),
    TEAMLER_BROADCAST("dreamveil.teamler.broadcast", "Can broadcast server messages"),
    TEAMLER_CLEARCHAT("dreamveil.teamler.clearchat", "Can clear the server chat"),
    TEAMLER_FREEZE("dreamveil.teamler.freeze", "Can freeze and unfreeze players"),
    TEAMLER_STAFFLIST("dreamveil.teamler.stafflist", "Can view the staff list"),

    // ==================== ADMIN COMMANDS ====================
    ADMIN_CLEARINVENTORY("dreamveil.admin.clearinventory", "Can clear player inventories"),
    ADMIN_SERVERINFO("dreamveil.admin.serverinfo", "Can view server info"),
    ADMIN_TPS("dreamveil.admin.tps", "Can view server TPS"),
    ADMIN_UPTIME("dreamveil.admin.uptime", "Can view server uptime"),
    RANK_GIVE("dreamveil.rank.give", "Can give normal ranks"),
    RANK_REMOVE("dreamveil.rank.remove", "Can remove normal ranks"),
    RANK_TEAM_GIVE("dreamveil.teamrank.give", "Can give team ranks"),
    RANK_TEAM_REMOVE("dreamveil.teamrank.remove", "Can remove team ranks"),
    ALL_PERMISSIONS("dreamveil.*", "All permissions");


    private final String permission;
    private final String description;

    Permission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    /**
     * Get permission string
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Get permission description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if player has this permission
     */
    public boolean hasPermission(org.bukkit.entity.Player player) {
        return player.hasPermission(this.permission);
    }

    /**
     * Check if player has this permission by UUID via API
     */
    public boolean hasPermissionByUUID(String uuid) {
        return de.yannik.dreamveilCore.api.DreamveilAPI.getRank()
                .hasPermission(uuid, this.permission);
    }

    /**
     * Get permission by string
     */
    public static Permission fromString(String perm) {
        for (Permission p : values()) {
            if (p.permission.equalsIgnoreCase(perm)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return permission;
    }
}