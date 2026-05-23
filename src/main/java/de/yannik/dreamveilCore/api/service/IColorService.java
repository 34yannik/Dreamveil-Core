package de.yannik.dreamveilCore.api.service;

import de.yannik.dreamveilCore.player.model.PlayerColor;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Public API for the Chat Color system.
 * External plugins access this via {@code DreamveilAPI.getColors()}.
 *
 * <h3>Design contract</h3>
 * <ul>
 *   <li>Synchronous getters always read from cache and are only valid for
 *       online players whose data has been loaded.</li>
 *   <li>Async methods are safe to call from any thread.  Callbacks run on
 *       the database-executor thread – reschedule to the main thread for
 *       Bukkit-API usage.</li>
 *   <li>Free colors ({@link PlayerColor#isFree()} == true) are always
 *       considered unlocked without a DB entry.</li>
 * </ul>
 */
public interface IColorService {

    // ── Unlock system ─────────────────────────────────────────────────────────

    /**
     * Load (or return cached) all unlocked colors for a player.
     * Free colors are always included in the result.
     *
     * @param uuid     Player UUID
     * @param callback Receives an unmodifiable set of unlocked colors
     */
    void loadUnlockedColorsAsync(String uuid, Consumer<Set<PlayerColor>> callback);

    /**
     * Fast, synchronous unlock check (cache only).
     * Always returns true for free colors.
     * Returns false on cache miss; prefer {@link #isColorUnlockedAsync} for offline players.
     *
     * @param uuid  Player UUID
     * @param color Color to check
     * @return true if unlocked (or free)
     */
    boolean isColorUnlocked(String uuid, PlayerColor color);

    /**
     * Async unlock check – falls back to a DB query on cache miss.
     *
     * @param uuid     Player UUID
     * @param color    Color to check
     * @param callback Receives true if unlocked or free
     */
    void isColorUnlockedAsync(String uuid, PlayerColor color, Consumer<Boolean> callback);

    /**
     * Unlock a color for a player (idempotent – safe if already unlocked).
     * Updates the cache immediately and persists to the DB asynchronously.
     *
     * @param uuid     Player UUID
     * @param color    Color to unlock
     * @param callback Called after DB write completes
     */
    void unlockColorAsync(String uuid, PlayerColor color, Runnable callback);

    /**
     * Returns a snapshot of the cached unlocked colors, or null on cache miss.
     *
     * @param uuid Player UUID
     * @return Unmodifiable set, or null if not cached
     */
    Set<PlayerColor> getUnlockedColors(String uuid);

    // ── Selection system ──────────────────────────────────────────────────────

    /**
     * Set the active chat-message color.
     * Rejected (callback false) if the color is not unlocked.
     *
     * @param uuid     Player UUID
     * @param color    Color to activate
     * @param callback true = success, false = not unlocked / error
     */
    void setChatMsgColorAsync(String uuid, PlayerColor color, Consumer<Boolean> callback);

    /**
     * Set the active chat-name color.
     * Rejected (callback false) if the color is not unlocked.
     *
     * @param uuid     Player UUID
     * @param color    Color to activate
     * @param callback true = success, false = not unlocked / error
     */
    void setChatNameColorAsync(String uuid, PlayerColor color, Consumer<Boolean> callback);

    /**
     * Get the currently active chat-message color (from cache).
     * Returns {@link PlayerColor#DEFAULT} if not cached.
     *
     * @param uuid Player UUID
     */
    PlayerColor getChatMsgColor(String uuid);

    /**
     * Get the currently active chat-name color (from cache).
     * Returns {@link PlayerColor#DEFAULT} if not cached.
     *
     * @param uuid Player UUID
     */
    PlayerColor getChatNameColor(String uuid);

    // ── Enum utility ──────────────────────────────────────────────────────────

    /**
     * Returns all defined color options.
     */
    PlayerColor[] getAllColors();
}