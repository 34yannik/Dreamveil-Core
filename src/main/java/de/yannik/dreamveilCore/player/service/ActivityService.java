package de.yannik.dreamveilCore.player.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.ActivityRepository;
import de.yannik.dreamveilCore.player.cache.PlayerCache;
import de.yannik.dreamveilCore.player.model.PlayerData;
import de.yannik.dreamveilCore.util.Log;

/**
 * Service layer for activity tracking.
 * Handles playtime and login streak management.
 * Updates both cache and database asynchronously.
 */
public class ActivityService {

    private static final ActivityRepository repository = new ActivityRepository();

    /**
     * Get playtime from cache (synchronous)
     */
    public static long getPlaytime(String uuid) {
        PlayerData player = PlayerCache.get(uuid);
        return player != null ? player.getPlaytime() : 0;
    }

    /**
     * Get login streak from cache (synchronous)
     */
    public static int getLoginStreak(String uuid) {
        PlayerData player = PlayerCache.get(uuid);
        return player != null ? player.getLoginStreak() : 0;
    }

    /**
     * Get longest streak from cache (synchronous)
     */
    public static int getLongestStreak(String uuid) {
        PlayerData player = PlayerCache.get(uuid);
        return player != null ? player.getLongestStreak() : 0;
    }

    /**
     * Add playtime and persist asynchronously
     */
    public static void addPlaytimeAsync(String uuid, long seconds, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setPlaytime(player.getPlaytime() + seconds);
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.addPlaytime(uuid, seconds);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to add playtime for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Set playtime and persist asynchronously
     */
    public static void setPlaytimeAsync(String uuid, long seconds, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setPlaytime(seconds);
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.updatePlaytime(uuid, seconds);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to set playtime for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Update login streak and persist asynchronously
     */
    public static void updateLoginStreakAsync(String uuid, int newStreak, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setLoginStreak(newStreak);
            
            // Update longest streak if this is higher
            if (newStreak > player.getLongestStreak()) {
                player.setLongestStreak(newStreak);
            }
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.updateLoginStreak(uuid, newStreak);
                
                PlayerData p = PlayerCache.get(uuid);
                if (p != null && newStreak > p.getLongestStreak()) {
                    repository.updateLongestStreak(uuid, newStreak);
                }

                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update streak for " + uuid + ": " + e.getMessage());
            }
        });
    }
}