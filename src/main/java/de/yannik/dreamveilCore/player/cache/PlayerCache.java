package de.yannik.dreamveilCore.player.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.yannik.dreamveilCore.player.model.PlayerData;
import de.yannik.dreamveilCore.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Thread-safe cache for player data using Caffeine.
 * Provides fast synchronous access to cached player information.
 * Cache access is intentionally synchronous - async loading happens at service layer.
 */
public class PlayerCache {

    private static final Cache<String, PlayerData> cache = Caffeine.newBuilder()
            .maximumSize(500)                           // Max 500 players in memory
            .expireAfterAccess(30, TimeUnit.MINUTES)   // Remove after 30 min inactivity
            .recordStats()                              // Track hit/miss rates
            .build();

    /**
     * Get player from cache (null if not present)
     */
    public static PlayerData get(String uuid) {
        PlayerData data = cache.getIfPresent(uuid);
        return data;
    }

    /**
     * Store player in cache
     */
    public static void put(String uuid, PlayerData data) {
        cache.put(uuid, data);
    }

    /**
     * Remove player from cache
     */
    public static void remove(String uuid) {
        cache.invalidate(uuid);
    }

    /**
     * Check if player is in cache
     */
    public static boolean contains(String uuid) {
        return cache.getIfPresent(uuid) != null;
    }

    /**
     * Clear entire cache
     */
    public static void clear() {
        cache.invalidateAll();
    }

    /**
     * Get cache statistics
     */
    public static String getStats() {
        return "Cache Stats - " + cache.stats().toString();
    }
}