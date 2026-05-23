package de.yannik.dreamveilCore.player.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.ColorRepository;
import de.yannik.dreamveilCore.player.cache.ColorCache;
import de.yannik.dreamveilCore.player.cache.SettingsCache;
import de.yannik.dreamveilCore.player.model.PlayerColor;
import de.yannik.dreamveilCore.player.model.PlayerSettings;
import de.yannik.dreamveilCore.util.Log;

import java.util.*;
import java.util.function.Consumer;

/**
 * Service layer for the Chat Color system.
 *
 * Responsibilities:
 *   1. Unlock management  – track which colors a player has earned
 *   2. Selection          – set/get the active gradient for name + message
 *   3. Validation         – a color may only be selected if it is unlocked
 *                           (or free/always-unlocked)
 *
 * Pattern contract (mirrors rest of codebase):
 *   • Synchronous reads always hit the cache (fast path).
 *   • Writes update the cache first, then persist async.
 *   • Async callbacks run on the database thread pool; callers must
 *     schedule any Bukkit-API work back to the main thread themselves.
 */
public class ColorService {

    private static final ColorRepository repository = new ColorRepository();

    // ──────────────────────────────────────────────────────────────────────────
    // UNLOCK SYSTEM
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load (or return cached) unlocked colors for a player asynchronously.
     * Free colors are automatically included in every result set.
     */
    public static void loadUnlockedColorsAsync(String uuid, Consumer<Set<PlayerColor>> callback) {
        Set<PlayerColor> cached = ColorCache.get(uuid);
        if (cached != null) {
            callback.accept(Collections.unmodifiableSet(cached));
            return;
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                List<PlayerColor> dbColors = repository.getUnlockedColors(uuid);

                Set<PlayerColor> allUnlocked = new LinkedHashSet<>();

                // Always include free colors
                for (PlayerColor c : PlayerColor.values()) {
                    if (c.isFree()) allUnlocked.add(c);
                }
                allUnlocked.addAll(dbColors);

                ColorCache.put(uuid, allUnlocked);
                callback.accept(Collections.unmodifiableSet(allUnlocked));
            } catch (Exception e) {
                Log.error("ColorService: failed to load colors for " + uuid + ": " + e.getMessage());
                callback.accept(Collections.emptySet());
            }
        });
    }

    /**
     * Synchronous unlocked-color check (cache only).
     * Returns false on cache miss – call {@link #loadUnlockedColorsAsync} first on login.
     */
    public static boolean isColorUnlocked(String uuid, PlayerColor color) {
        if (color.isFree()) return true;
        return ColorCache.hasColor(uuid, color);
    }

    /**
     * Async existence check – falls back to DB on cache miss.
     * Prefer synchronous version for online players (always cached).
     */
    public static void isColorUnlockedAsync(String uuid, PlayerColor color, Consumer<Boolean> callback) {
        if (color.isFree()) {
            callback.accept(true);
            return;
        }

        // Fast path: cache hit
        if (ColorCache.isCached(uuid)) {
            callback.accept(ColorCache.hasColor(uuid, color));
            return;
        }

        // Slow path: single DB query without loading full set
        DatabaseExecutor.runAsync(() -> {
            try {
                boolean unlocked = repository.isColorUnlocked(uuid, color);
                callback.accept(unlocked);
            } catch (Exception e) {
                Log.error("ColorService: failed to check color " + color.name()
                        + " for " + uuid + ": " + e.getMessage());
                callback.accept(false);
            }
        });
    }

    /**
     * Unlock a color for a player.
     * Updates the cache immediately; DB write is async.
     * Safe to call even if already unlocked (idempotent via INSERT IGNORE).
     */
    public static void unlockColorAsync(String uuid, PlayerColor color, Runnable callback) {
        // Eager cache update
        ColorCache.addColor(uuid, color);

        DatabaseExecutor.runAsync(() -> {
            try {
                repository.unlockColor(uuid, color);
                Log.info("ColorService: unlocked " + color.name() + " for " + uuid);
                callback.run();
            } catch (Exception e) {
                Log.error("ColorService: failed to unlock color " + color.name()
                        + " for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Return a snapshot of currently unlocked colors (from cache).
     * Returns null on cache miss – caller should use async variant.
     */
    public static Set<PlayerColor> getUnlockedColors(String uuid) {
        Set<PlayerColor> cached = ColorCache.get(uuid);
        return cached != null ? Collections.unmodifiableSet(cached) : null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SELECTION SYSTEM
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Set the active chat-message color.
     *
     * Validation:
     *   – Free colors are always accepted.
     *   – Non-free colors must be present in the cache/DB.
     *
     * Callback receives {@code true} on success, {@code false} on rejection.
     */
    public static void setChatMsgColorAsync(String uuid, PlayerColor color, Consumer<Boolean> callback) {
        setColorAsync(uuid, color, callback, false);
    }

    /**
     * Set the active chat-name color.
     * Same validation semantics as {@link #setChatMsgColorAsync}.
     */
    public static void setChatNameColorAsync(String uuid, PlayerColor color, Consumer<Boolean> callback) {
        setColorAsync(uuid, color, callback, true);
    }

    /**
     * Synchronous read from cache.
     * Returns {@link PlayerColor#DEFAULT} if not cached.
     */
    public static PlayerColor getChatMsgColor(String uuid) {
        PlayerSettings settings = SettingsCache.get(uuid);
        return settings != null ? settings.getChatMsgColor() : PlayerColor.DEFAULT;
    }

    /**
     * Synchronous read from cache.
     * Returns {@link PlayerColor#DEFAULT} if not cached.
     */
    public static PlayerColor getChatNameColor(String uuid) {
        PlayerSettings settings = SettingsCache.get(uuid);
        return settings != null ? settings.getChatNameColor() : PlayerColor.DEFAULT;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CACHE MANAGEMENT
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Evict a player's color cache on logout.
     */
    public static void invalidate(String uuid) {
        ColorCache.remove(uuid);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Shared implementation for setChatMsgColor / setChatNameColor.
     *
     * @param isNameColor true → updates chatNameColor; false → chatMsgColor
     */
    private static void setColorAsync(String uuid, PlayerColor color,
                                      Consumer<Boolean> callback, boolean isNameColor) {
        // Validate unlock
        isColorUnlockedAsync(uuid, color, unlocked -> {
            if (!unlocked) {
                Log.warn("ColorService: " + uuid + " tried to select locked color "
                        + color.name() + " – rejected.");
                callback.accept(false);
                return;
            }

            // Update settings cache immediately
            PlayerSettings settings = SettingsCache.get(uuid);
            if (settings != null) {
                if (isNameColor) settings.setChatNameColor(color);
                else             settings.setChatMsgColor(color);
            }

            // Persist async
            DatabaseExecutor.runAsync(() -> {
                try {
                    if (isNameColor) repository.updateChatNameColor(uuid, color);
                    else             repository.updateChatMsgColor(uuid, color);
                    callback.accept(true);
                } catch (Exception e) {
                    Log.error("ColorService: failed to persist color selection for "
                            + uuid + ": " + e.getMessage());
                    callback.accept(false);
                }
            });
        });
    }
}