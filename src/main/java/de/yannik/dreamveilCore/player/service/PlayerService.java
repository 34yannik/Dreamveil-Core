package de.yannik.dreamveilCore.player.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.PlayerRepository;
import de.yannik.dreamveilCore.player.cache.PlayerCache;
import de.yannik.dreamveilCore.player.model.PlayerData;
import de.yannik.dreamveilCore.util.Log;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Service layer for player core operations.
 * Mit Smart Cache Loading
 */
public class PlayerService {

    private static final PlayerRepository repository = new PlayerRepository();

    /**
     * Get or Load Player (Smart Cache)
     * - Wenn im Cache → sofort zurück
     * - Wenn NOT im Cache → async laden und cachen
     *
     * @param uuid Player UUID
     * @param callback Called when data is available
     */
    public static void getOrLoadAsync(String uuid, Consumer<PlayerData> callback) {
        // Zuerst Cache checken
        PlayerData cached = PlayerCache.get(uuid);
        if (cached != null) {
            // Im Cache → sofort callback
            callback.accept(cached);
            return;
        }

        // NOT im Cache → async laden
        loadPlayerAsync(uuid, callback);
    }

    /**
     * Load player data asynchronously
     * Lädt aus DB und speichert im Cache
     *
     * @param uuid Player UUID
     * @param callback Called when data is loaded
     */
    public static void loadPlayerAsync(String uuid, Consumer<PlayerData> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                PlayerData player = repository.loadPlayer(uuid);

                if (player != null) {
                    // In Cache speichern
                    PlayerCache.put(uuid, player);
                } else {
                    Log.warn("Player not found in database: " + uuid);
                }

                callback.accept(player);
            } catch (Exception e) {
                Log.error("Failed to load player " + uuid + ": " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    /**
     * Get player from cache only (fast, non-blocking)
     * Gibt null zurück wenn nicht im Cache
     */
    public static PlayerData getPlayer(String uuid) {
        return PlayerCache.get(uuid);
    }

    /**
     * Create new player asynchronously
     */
    public static void createPlayerAsync(String uuid, String username, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                if (repository.playerExists(uuid)) {
                    Log.warn("Player already exists: " + uuid);
                    callback.run();
                    return;
                }

                repository.createPlayer(uuid, username);

                PlayerData player = new PlayerData(uuid);
                player.setUsername(username);
                player.setFirstLogin(LocalDateTime.now());
                player.setLastLogin(LocalDateTime.now());

                PlayerCache.put(uuid, player);

                callback.run();
            } catch (Exception e) {
                Log.error("Failed to create player " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Save player data asynchronously
     */
    public static void savePlayerAsync(String uuid, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                PlayerData player = PlayerCache.get(uuid);
                if (player == null) {
                    Log.warn("Cannot save player - not in cache: " + uuid);
                    callback.run();
                    return;
                }

                repository.updatePlayerCore(player);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to save player " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Update last logout timestamp asynchronously
     */
    public static void updateLastLogoutAsync(String uuid, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.updateLastLogout(uuid);

                PlayerData player = PlayerCache.get(uuid);
                if (player != null) {
                    player.setLastLogout(LocalDateTime.now());
                }

                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update logout for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Remove player from cache
     */
    public static void unloadPlayer(String uuid) {
        PlayerCache.remove(uuid);
    }
}