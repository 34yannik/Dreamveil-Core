package de.yannik.dreamveilCore.rank.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.yannik.dreamveilCore.rank.model.PlayerRank;
import de.yannik.dreamveilCore.rank.model.PlayerRankData;
import de.yannik.dreamveilCore.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache for player ranks - high performance lookups
 */
public class RankCache {

    private static final Cache<String, List<PlayerRankData>> rankCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

    private static final Cache<String, PlayerRank> displayRankCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    /**
     * Get all ranks for player
     */
    public static List<PlayerRankData> getRanks(String uuid) {
        return rankCache.getIfPresent(uuid);
    }

    /**
     * Cache player ranks
     */
    public static void putRanks(String uuid, List<PlayerRankData> ranks) {
        rankCache.put(uuid, ranks);
        Log.info("Cached ranks for " + uuid + ": " + ranks.size() + " rank(s)");
    }

    /**
     * Get highest priority rank (display rank)
     */
    public static PlayerRank getDisplayRank(String uuid) {
        return displayRankCache.getIfPresent(uuid);
    }

    /**
     * Cache display rank
     */
    public static void putDisplayRank(String uuid, PlayerRank rank) {
        displayRankCache.put(uuid, rank);
    }

    /**
     * Invalidate all ranks for player (on update/logout)
     */
    public static void invalidate(String uuid) {
        rankCache.invalidate(uuid);
        displayRankCache.invalidate(uuid);
        Log.info("Invalidated rank cache for " + uuid);
    }

    /**
     * Clear entire cache
     */
    public static void clear() {
        rankCache.invalidateAll();
        displayRankCache.invalidateAll();
        Log.info("Rank cache cleared");
    }

    /**
     * Get cache stats
     */
    public static String getStats() {
        return "Rank Cache - " + rankCache.stats().toString();
    }
}