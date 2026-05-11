package de.yannik.dreamveilCore.rank.permission;

/**
 * All permissions used in DreamveilCore and related plugins
 * Type-safe permission management
 */
public enum Permission {

    // ==================== TEAMLER ====================
    TEAMLER_CHAT("dreamveil.teamler.chat", "Can use teamchat"),
    TEAMLER_DUTY("dreamveil.teamler.duty", "Join team duty"),

    // ==================== ADMIN ====================
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