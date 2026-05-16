package de.yannik.dreamveilCore.player.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.TitleRepository;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.util.Log;

import java.util.List;
import java.util.function.Consumer;

public class TitleService {

    // ==================== READ ====================

    /**
     * Load all unlocked titles for a player asynchronously.
     */
    public static void loadUnlockedAsync(String uuid, Consumer<List<Title>> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                List<Title> titles = TitleRepository.loadUnlocked(uuid);
                callback.accept(titles);
            } catch (Exception e) {
                Log.error("Failed to load titles for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Check asynchronously whether a player has a specific title.
     */
    public static void hasTitleAsync(String uuid, Title title, Consumer<Boolean> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                boolean has = TitleRepository.hasTitle(uuid, title);
                callback.accept(has);
            } catch (Exception e) {
                Log.error("Failed to check title " + title.name() + " for " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== WRITE ====================

    /**
     * Unlock a title for a player. Safe to call if already unlocked (no-op).
     */
    public static void unlockAsync(String uuid, Title title, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                TitleRepository.unlock(uuid, title);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to unlock title " + title.name() + " for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Revoke a specific title from a player.
     */
    public static void revokeAsync(String uuid, Title title, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                TitleRepository.revoke(uuid, title);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to revoke title " + title.name() + " for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Revoke all titles from a player.
     */
    public static void revokeAllAsync(String uuid, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                TitleRepository.revokeAll(uuid);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to revoke all titles for " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== UTILITY ====================

    /** All title definitions from the enum. */
    public static Title[] getAllTitles() {
        return Title.values();
    }
}