package de.yannik.dreamveilCore.api.service;

import de.yannik.dreamveilCore.player.model.Title;

import java.util.List;
import java.util.function.Consumer;

/**
 * Public API for player titles.
 * External plugins use this interface via {@code DreamveilAPI.getTitles()}.
 */
public interface ITitleService {

    /**
     * Get all unlocked titles for a player asynchronously.
     */
    void loadUnlockedAsync(String uuid, Consumer<List<Title>> callback);

    /**
     * Check whether a player has a specific title unlocked.
     */
    void hasTitleAsync(String uuid, Title title, Consumer<Boolean> callback);

    /**
     * Unlock a title for a player. Safe to call if already unlocked.
     */
    void unlockAsync(String uuid, Title title, Runnable callback);

    /**
     * Revoke a specific title from a player.
     */
    void revokeAsync(String uuid, Title title, Runnable callback);

    /**
     * Revoke all titles from a player.
     */
    void revokeAllAsync(String uuid, Runnable callback);

    /**
     * Returns all title definitions from the enum.
     */
    Title[] getAllTitles();
}