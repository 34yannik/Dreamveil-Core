package de.yannik.dreamveilCore.api.service;

/**
 * Public API interface for activity tracking.
 * External plugins must use this interface, not the implementation.
 * 
 * All operations are asynchronous and persist to database.
 */
public interface IActivityService {

    /**
     * Get player playtime from cache (synchronous)
     * Only works for online players
     * 
     * @param uuid Player UUID
     * @return Playtime in milliseconds or 0 if not cached
     */
    long getPlaytime(String uuid);

    /**
     * Get player login streak from cache (synchronous)
     * 
     * @param uuid Player UUID
     * @return Current streak or 0 if not cached
     */
    int getLoginStreak(String uuid);

    /**
     * Get player longest streak from cache (synchronous)
     * 
     * @param uuid Player UUID
     * @return Longest streak or 0 if not cached
     */
    int getLongestStreak(String uuid);

    /**
     * Add playtime asynchronously
     * 
     * @param uuid Player UUID
     * @param milliseconds Playtime to add
     * @param callback Called when database save completes
     */
    void addPlaytimeAsync(String uuid, long milliseconds, Runnable callback);

    /**
     * Set playtime asynchronously
     * 
     * @param uuid Player UUID
     * @param milliseconds New playtime
     * @param callback Called when database save completes
     */
    void setPlaytimeAsync(String uuid, long milliseconds, Runnable callback);

    /**
     * Update login streak asynchronously
     * Automatically updates longest_streak if needed
     * 
     * @param uuid Player UUID
     * @param newStreak New streak value
     * @param callback Called when database save completes
     */
    void updateLoginStreakAsync(String uuid, int newStreak, Runnable callback);
}