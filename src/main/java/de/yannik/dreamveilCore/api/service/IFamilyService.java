package de.yannik.dreamveilCore.api.service;

import de.yannik.dreamveilCore.family.model.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Public API for the Found Family system.
 * External plugins access this via {@code DreamveilAPI.getFamilies()}.
 */
public interface IFamilyService {

    /** Find a family by its display name (case-insensitive). Callback receives null if not found. */
    void loadFamilyByNameAsync(String name, Consumer<Family> callback);

    /** Load all families – used by the browser GUI. Always fetches fresh data. */
    void getAllFamiliesAsync(Consumer<List<Family>> callback);

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * Load (or return cached) the family a player belongs to.
     * Callback receives null if the player has no family.
     */
    void loadFamilyByPlayerAsync(String playerUuid, Consumer<Family> callback);

    /**
     * Load (or return cached) a family by its ID.
     * Callback receives null if not found.
     */
    void loadFamilyByIdAsync(String familyId, Consumer<Family> callback);

    /**
     * Synchronous family lookup from cache only.
     * Returns null on cache miss.
     */
    Family getFamily(String familyId);

    /**
     * Returns the cached familyId for a player (may be null = no family).
     * Returns null if not yet loaded into cache.
     */
    String getFamilyIdForPlayer(String playerUuid);

    // ── Create / Delete ───────────────────────────────────────────────────────

    /**
     * Create a new family owned by the given player.
     *
     * Guards (enforced asynchronously):
     *   – Owner must not already be in a family.
     *   – Family name must be unique.
     *   – Tag must be unique, 1–5 alphanumeric characters.
     *     Validated synchronously before the DB call; callback receives null on rejection.
     *
     * @param tag      Short identifier, max 5 chars, alphanumeric, stored uppercase
     * @param callback Receives the new family ID on success, null on rejection/error.
     */
    void createFamilyAsync(String ownerUuid, String ownerName,
                           String name, String tag, String description,
                           Consumer<String> callback);

    /**
     * Disband a family and remove all members.
     * Permission checks are the caller's responsibility.
     */
    void deleteFamilyAsync(String familyId, Runnable callback);

    // ── Members ───────────────────────────────────────────────────────────────

    /**
     * Load all members of a family (with caching).
     */
    void loadMembersAsync(String familyId, Consumer<List<FamilyMember>> callback);

    /**
     * Add a player to a family as MEMBER.
     * Guard: player must not already be in any family.
     *
     * @param callback true = success, false = guard rejected / error
     */
    void addMemberAsync(String familyId, String playerUuid,
                        String playerName, Consumer<Boolean> callback);

    /**
     * Remove a player from a family.
     */
    void removeMemberAsync(String familyId, String playerUuid, Runnable callback);

    /**
     * Update a member's role (e.g. promote to CO_OWNER).
     */
    void updateMemberRoleAsync(String familyId, String playerUuid,
                               FamilyRole role, Runnable callback);

    // ── Home ──────────────────────────────────────────────────────────────────

    /**
     * Load the family home. Callback receives null if none is set.
     */
    void loadHomeAsync(String familyId, Consumer<FamilyHome> callback);

    /**
     * Set (or replace) the family home location.
     *
     * @param setByUuid UUID of the member setting the home
     */
    void setHomeAsync(String familyId, String world,
                      double x, double y, double z,
                      float yaw, float pitch,
                      String setByUuid, Runnable callback);

    // ── Buffs ─────────────────────────────────────────────────────────────────

    /**
     * Load all active (non-expired) buffs for a family.
     */
    void loadActiveBuffsAsync(String familyId, Consumer<List<FamilyActiveBuff>> callback);

    /**
     * Activate a buff.  Duration is derived from buff level via
     * {@link FamilyBuff#getEffectiveDurationHours(int)}.
     */
    void activateBuffAsync(String familyId, FamilyBuff buffType,
                           int level, String activatedByUuid,
                           Runnable callback);

    // ── Enum utility ──────────────────────────────────────────────────────────

    FamilyBuff[] getAllBuffTypes();
    FamilyRole[] getAllRoles();
}