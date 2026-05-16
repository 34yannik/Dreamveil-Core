package de.yannik.dreamveilCore.player.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.SettingsRepository;
import de.yannik.dreamveilCore.player.cache.SettingsCache;
import de.yannik.dreamveilCore.player.model.PlayerSettings;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.util.Log;

import java.util.function.Consumer;

public class SettingsService {

    // ==================== READ ====================

    /**
     * Get settings from cache (synchronous).
     * Returns null if the player is not cached yet.
     */
    public static PlayerSettings getSettings(String uuid) {
        return SettingsCache.get(uuid);
    }

    /**
     * Load settings from DB into cache asynchronously.
     * Creates a default row if the player has none yet.
     */
    public static void loadSettingsAsync(String uuid, Consumer<PlayerSettings> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                PlayerSettings settings = SettingsRepository.load(uuid);

                if (settings == null) {
                    SettingsRepository.insertDefault(uuid);
                    settings = PlayerSettings.defaultFor(uuid);
                    Log.info("SettingsService: Created default settings for " + uuid);
                }

                SettingsCache.put(uuid, settings);
                callback.accept(settings);
            } catch (Exception e) {
                Log.error("Failed to load settings for " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== PRONOUNS ====================

    public static void setPronounsAsync(String uuid, String pronouns, Runnable callback) {
        // Update cache immediately
        PlayerSettings settings = SettingsCache.get(uuid);
        if (settings != null) {
            settings.setPronouns(pronouns);
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                SettingsRepository.updatePronouns(uuid, pronouns);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update pronouns for " + uuid + ": " + e.getMessage());
            }
        });
    }

    public static void setShowPronounsAsync(String uuid, boolean showPronouns, Runnable callback) {
        // Update cache immediately
        PlayerSettings settings = SettingsCache.get(uuid);
        if (settings != null) {
            settings.setShowPronouns(showPronouns);
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                SettingsRepository.updateShowPronouns(uuid, showPronouns);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update show_pronouns for " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== TITLE ====================

    /**
     * Set the active (selected) title. Pass null to clear.
     */
    public static void setSelectedTitleAsync(String uuid, Title title, Runnable callback) {
        // Update cache immediately
        PlayerSettings settings = SettingsCache.get(uuid);
        if (settings != null) {
            settings.setSelectedTitle(title);
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                SettingsRepository.updateSelectedTitle(uuid, title);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to update selected_title for " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== FULL SAVE ====================

    /**
     * Persist a full PlayerSettings object (e.g. after batch edits).
     */
    public static void saveAsync(PlayerSettings settings, Runnable callback) {
        // Ensure cache stays in sync
        SettingsCache.put(settings.getUuid(), settings);

        DatabaseExecutor.runAsync(() -> {
            try {
                SettingsRepository.save(settings);
                callback.run();
            } catch (Exception e) {
                Log.error("Failed to save settings for " + settings.getUuid() + ": " + e.getMessage());
            }
        });
    }

    // ==================== CACHE CLEANUP ====================

    public static void invalidate(String uuid) {
        SettingsCache.remove(uuid);
    }
}