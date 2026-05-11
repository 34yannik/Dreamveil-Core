package de.yannik.dreamveilCore.player.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.EconomyRepository;
import de.yannik.dreamveilCore.player.cache.PlayerCache;
import de.yannik.dreamveilCore.player.model.PlayerData;
import de.yannik.dreamveilCore.util.Log;

/**
 * Service layer for economy operations.
 * Handles balance and shards management.
 * Updates both cache and database asynchronously.
 */
public class EconomyService {

    private static final EconomyRepository repository = new EconomyRepository();

    /**
     * Get current balance from cache (synchronous)
     */
    public static long getBalance(String uuid) {
        PlayerData player = PlayerCache.get(uuid);
        return player != null ? player.getBalance() : 0;
    }

    /**
     * Get current shards from cache (synchronous)
     */
    public static long getShards(String uuid) {
        PlayerData player = PlayerCache.get(uuid);
        return player != null ? player.getShards() : 0;
    }

    /**
     * Set balance and persist asynchronously
     */
    public static void setBalanceAsync(String uuid, long amount, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setBalance(amount);
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.updateBalance(uuid, amount);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update balance for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Add balance and persist asynchronously
     */
    public static void addBalanceAsync(String uuid, long amount, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setBalance(player.getBalance() + amount);
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.addBalance(uuid, amount);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to add balance for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Set shards and persist asynchronously
     */
    public static void setShardsAsync(String uuid, long amount, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setShards(amount);
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.updateShards(uuid, amount);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update shards for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Add shards and persist asynchronously
     */
    public static void addShardsAsync(String uuid, long amount, Runnable callback) {
        // Update cache immediately
        PlayerData player = PlayerCache.get(uuid);
        if (player != null) {
            player.setShards(player.getShards() + amount);
        }

        // Persist asynchronously
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.addShards(uuid, amount);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to add shards for " + uuid + ": " + e.getMessage());
            }
        });
    }
}