package de.yannik.dreamveilCore.api.service;

import de.yannik.dreamveilCore.rank.model.PlayerRank;
import java.util.List;
import java.util.function.Consumer;

/**
 * Public API for rank operations
 * External plugins use this interface
 */
public interface IRankService {

    /**
     * Get display rank for player (highest priority active rank)
     */
    PlayerRank getDisplayRank(String uuid);

    /**
     * Get display name with gradient
     */
    String getDisplayName(String uuid);

    /**
     * Check if player has a specific permission
     */
    boolean hasPermission(String uuid, String permission);

    /**
     * Get all permissions for player
     */
    String[] getAllPermissions(String uuid);

    /**
     * Load ranks asynchronously (callback when done)
     */
    void loadRanksAsync(String uuid, Consumer<Object> callback);

    /**
     * Get all rank definitions
     */
    PlayerRank[] getAllRanks();

    /**
     * Check if rank is permanent or has expiry
     */
    boolean isRankPermanent(String uuid);

    void addRankAsync(String uuid, String playerName, PlayerRank rank,
                      int durationDays, String grantedBy, Runnable callback);

    void removeRankAsync(String uuid, String playerName, PlayerRank rank,
                         String revokedBy, Runnable callback);

    void checkExpiredRanksAsync(String uuid, String playerName, Runnable callback);
}