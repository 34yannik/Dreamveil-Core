package de.yannik.dreamveilCore.api.service;

import de.yannik.dreamveilCore.player.model.PlayerData;

import java.util.function.Consumer;

/**
 * Public API interface for player operations.
 * External plugins must use this interface, not the implementation.
 * 
 * All operations are asynchronous and non-blocking.
 */
public interface IPlayerService {

    /**
     * Load player data asynchronously
     * 
     * @param uuid Player UUID
     * @param callback Called when data is loaded (or null if not found)
     */
    void loadPlayerAsync(String uuid, Consumer<PlayerData> callback);

    /**
     * Get player from cache (synchronous, only works if online)
     * Returns null if not in cache
     * 
     * @param uuid Player UUID
     * @return PlayerData or null
     */
    PlayerData getPlayer(String uuid);

    /**
     * Check if player data is currently cached
     * 
     * @param uuid Player UUID
     * @return true if in cache
     */
    boolean isPlayerCached(String uuid);
}