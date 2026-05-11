package de.yannik.dreamveilCore.api.service;

/**
 * Public API interface for economy operations.
 * External plugins must use this interface, not the implementation.
 * 
 * All modifications are asynchronous and persist to database.
 */
public interface IEconomyService {

    /**
     * Get player balance from cache (synchronous)
     * Only works for online players
     * 
     * @param uuid Player UUID
     * @return Balance or 0 if not cached
     */
    long getBalance(String uuid);

    /**
     * Get player shards from cache (synchronous)
     * Only works for online players
     * 
     * @param uuid Player UUID
     * @return Shards or 0 if not cached
     */
    long getShards(String uuid);

    /**
     * Add balance asynchronously
     * Updates cache immediately, persists to database
     * 
     * @param uuid Player UUID
     * @param amount Amount to add
     * @param callback Called when database save completes
     */
    void addBalanceAsync(String uuid, long amount, Runnable callback);

    /**
     * Set balance asynchronously
     * 
     * @param uuid Player UUID
     * @param amount New balance
     * @param callback Called when database save completes
     */
    void setBalanceAsync(String uuid, long amount, Runnable callback);

    /**
     * Add shards asynchronously
     * 
     * @param uuid Player UUID
     * @param amount Amount to add
     * @param callback Called when database save completes
     */
    void addShardsAsync(String uuid, long amount, Runnable callback);

    /**
     * Set shards asynchronously
     * 
     * @param uuid Player UUID
     * @param amount New shards
     * @param callback Called when database save completes
     */
    void setShardsAsync(String uuid, long amount, Runnable callback);
}