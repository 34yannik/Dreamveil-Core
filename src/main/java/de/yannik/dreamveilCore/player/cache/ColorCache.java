package de.yannik.dreamveilCore.player.cache;

import de.yannik.dreamveilCore.player.model.PlayerColor;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for each player's set of unlocked {@link PlayerColor}s.
 *
 * Lifecycle:
 *   • Populated on player login by {@code ColorService.loadUnlockedColorsAsync}
 *   • Invalidated on player logout via {@code ColorService.invalidate}
 *   • Updated eagerly on unlock so the next read is always cache-served
 */
public class ColorCache {

    /** uuid → set of unlocked colors */
    private static final ConcurrentHashMap<String, Set<PlayerColor>> cache = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Store (or replace) the full unlocked-color set for a player.
     * The set is wrapped in a thread-safe view internally.
     */
    public static void put(String uuid, Set<PlayerColor> colors) {
        Set<PlayerColor> safeSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        safeSet.addAll(colors);
        cache.put(uuid, safeSet);
    }

    /**
     * Returns the cached color set, or {@code null} if not loaded yet.
     * Callers must treat null as "cache miss, load from DB".
     */
    public static Set<PlayerColor> get(String uuid) {
        return cache.get(uuid);
    }

    /**
     * Eagerly add a single color to an already-cached set.
     * If the player is not cached yet, this is a no-op
     * (the full set will be loaded from DB on next access).
     */
    public static void addColor(String uuid, PlayerColor color) {
        cache.computeIfPresent(uuid, (k, set) -> {
            set.add(color);
            return set;
        });
    }

    /**
     * Synchronous existence check (returns false on cache miss).
     */
    public static boolean hasColor(String uuid, PlayerColor color) {
        Set<PlayerColor> set = cache.get(uuid);
        return set != null && set.contains(color);
    }

    /**
     * True if the player's colors have been loaded into the cache.
     */
    public static boolean isCached(String uuid) {
        return cache.containsKey(uuid);
    }

    /** Remove player from cache (logout / invalidation). */
    public static void remove(String uuid) {
        cache.remove(uuid);
    }

    /** Full reset (plugin disable). */
    public static void clear() {
        cache.clear();
    }
}