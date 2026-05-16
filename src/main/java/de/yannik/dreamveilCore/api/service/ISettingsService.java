package de.yannik.dreamveilCore.api.service;

import de.yannik.dreamveilCore.player.model.PlayerSettings;
import de.yannik.dreamveilCore.player.model.Title;

import java.util.function.Consumer;

/**
 * Public API for player settings.
 * External plugins use this interface via {@code DreamveilAPI.getSettings()}.
 */
public interface ISettingsService {

    /**
     * Load settings for a player asynchronously.
     * Creates default settings if the player has no row yet.
     */
    void loadSettingsAsync(String uuid, Consumer<PlayerSettings> callback);

    /**
     * Update the player's pronouns.
     */
    void setPronounsAsync(String uuid, String pronouns, Runnable callback);

    /**
     * Toggle pronoun visibility.
     */
    void setShowPronounsAsync(String uuid, boolean showPronouns, Runnable callback);

    /**
     * Set the player's selected (active) title.
     * Pass {@code null} to clear the selection.
     */
    void setSelectedTitleAsync(String uuid, Title title, Runnable callback);

    /**
     * Persist a full PlayerSettings object back to the database.
     */
    void saveAsync(PlayerSettings settings, Runnable callback);
}