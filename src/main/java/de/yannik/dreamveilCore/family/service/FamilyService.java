package de.yannik.dreamveilCore.family.service;

import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.FamilyRepository;
import de.yannik.dreamveilCore.family.model.*;
import de.yannik.dreamveilCore.util.Log;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service layer for the Found Family system.
 *
 * In-memory cache: family objects + member lists are cached by familyId.
 * Player→familyId mapping is cached separately for fast lookups.
 *
 * Cache is intentionally simple (ConcurrentHashMap); a Caffeine cache
 * can be introduced later if the server population warrants it.
 */
public class FamilyService {

    private static final FamilyRepository repository = new FamilyRepository();

    // familyId → Family
    private static final Map<String, Family>           familyCache  = new ConcurrentHashMap<>();
    // familyId → member list
    private static final Map<String, List<FamilyMember>> memberCache = new ConcurrentHashMap<>();
    // playerUuid → familyId  (null value means "confirmed: no family")
    private static final Map<String, String>           playerFamily  = new ConcurrentHashMap<>();

    // ──────────────────────────────────────────────────────────────────────────
    // LOOKUP
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Find a family by name asynchronously. Callback receives null if not found.
     */
    public static void loadFamilyByNameAsync(String name, Consumer<Family> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                Family family = repository.getFamilyByName(name);
                if (family != null) familyCache.put(family.getId(), family);
                callback.accept(family);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load family by name '" + name + "': " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    /**
     * Load all families for the browser GUI (always fresh, not cached).
     */
    public static void getAllFamiliesAsync(Consumer<List<Family>> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                List<Family> families = repository.getAllFamilies();
                callback.accept(families);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load all families: " + e.getMessage());
                callback.accept(List.of());
            }
        });
    }

    /**
     * Load the family of a player asynchronously (with caching).
     * Callback receives null if the player has no family.
     */
    public static void loadFamilyByPlayerAsync(String playerUuid, Consumer<Family> callback) {
        // Cache hit: player family mapping already known
        if (playerFamily.containsKey(playerUuid)) {
            String fid = playerFamily.get(playerUuid);
            callback.accept(fid != null ? familyCache.get(fid) : null);
            return;
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                Family family = repository.getFamilyByPlayer(playerUuid);
                if (family != null) {
                    familyCache.put(family.getId(), family);
                    playerFamily.put(playerUuid, family.getId());
                } else {
                    playerFamily.put(playerUuid, null); // cache negative lookup
                }
                callback.accept(family);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load family for " + playerUuid + ": " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    /**
     * Load a family directly by ID (with caching).
     */
    public static void loadFamilyByIdAsync(String familyId, Consumer<Family> callback) {
        Family cached = familyCache.get(familyId);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                Family family = repository.getFamilyById(familyId);
                if (family != null) familyCache.put(familyId, family);
                callback.accept(family);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load family " + familyId + ": " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    /**
     * Get a family from cache only (null on miss).
     */
    public static Family getFamily(String familyId) {
        return familyCache.get(familyId);
    }

    /**
     * Get the cached familyId for a player, or null if not cached / no family.
     */
    public static String getFamilyIdForPlayer(String playerUuid) {
        return playerFamily.get(playerUuid);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE / DELETE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Create a new family.
     * Callback receives the new family ID on success, null on failure.
     *
     * Validation performed here:
     *   – Owner must not already be in a family.
     *   – Family name must be unique.
     */
    public static void createFamilyAsync(String ownerUuid, String ownerName,
                                         String name, String tag, String description,
                                         Consumer<String> callback) {
        // Validate tag before hitting the DB
        try {
            Family.validateTag(tag);
        } catch (IllegalArgumentException e) {
            Log.warn("FamilyService: invalid tag '" + tag + "': " + e.getMessage());
            callback.accept(null);
            return;
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                // Guard: already in a family
                if (repository.isInAnyFamily(ownerUuid)) {
                    Log.warn("FamilyService: " + ownerUuid + " tried to create a family while already in one.");
                    callback.accept(null);
                    return;
                }
                // Guard: name taken
                if (repository.nameExists(name)) {
                    Log.warn("FamilyService: family name '" + name + "' is already taken.");
                    callback.accept(null);
                    return;
                }
                // Guard: tag taken
                if (repository.tagExists(tag)) {
                    Log.warn("FamilyService: family tag '" + tag + "' is already taken.");
                    callback.accept(null);
                    return;
                }

                String familyId = repository.createFamily(ownerUuid, ownerName, name, tag, description);

                // Prime cache
                Family family = repository.getFamilyById(familyId);
                if (family != null) {
                    familyCache.put(familyId, family);
                    playerFamily.put(ownerUuid, familyId);
                }
                memberCache.remove(familyId);

                Log.info("FamilyService: family '" + name + "' [" + tag.toUpperCase() + "] created by " + ownerUuid);
                callback.accept(familyId);
            } catch (Exception e) {
                Log.error("FamilyService: failed to create family: " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    /**
     * Disband a family.  Only the owner should be permitted to do this
     * (caller is responsible for the permission check).
     */
    public static void deleteFamilyAsync(String familyId, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                // Evict all member→family mappings from the player cache
                List<FamilyMember> members = memberCache.getOrDefault(familyId, List.of());
                for (FamilyMember m : members) {
                    playerFamily.remove(m.getPlayerUuid());
                }

                repository.deleteFamily(familyId);

                familyCache.remove(familyId);
                memberCache.remove(familyId);

                Log.info("FamilyService: family " + familyId + " disbanded.");
                callback.run();
            } catch (Exception e) {
                Log.error("FamilyService: failed to delete family " + familyId + ": " + e.getMessage());
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MEMBERS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load all members of a family asynchronously (with caching).
     */
    public static void loadMembersAsync(String familyId, Consumer<List<FamilyMember>> callback) {
        List<FamilyMember> cached = memberCache.get(familyId);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                List<FamilyMember> members = repository.getMembers(familyId);
                memberCache.put(familyId, members);
                callback.accept(members);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load members for " + familyId + ": " + e.getMessage());
                callback.accept(List.of());
            }
        });
    }

    /**
     * Add a player to a family (as MEMBER role).
     * Guards: player must not be in another family.
     * Callback: true on success, false on rejection/error.
     */
    public static void addMemberAsync(String familyId, String playerUuid,
                                      String playerName, Consumer<Boolean> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                if (repository.isInAnyFamily(playerUuid)) {
                    Log.warn("FamilyService: " + playerUuid + " tried to join family " + familyId
                            + " but is already in one.");
                    callback.accept(false);
                    return;
                }

                repository.addMember(familyId, playerUuid, playerName, FamilyRole.MEMBER);
                playerFamily.put(playerUuid, familyId);
                memberCache.remove(familyId); // invalidate stale list

                // Update member count in family cache
                Family family = familyCache.get(familyId);
                if (family != null) family.setMemberCount(family.getMemberCount() + 1);

                Log.info("FamilyService: " + playerUuid + " joined family " + familyId);
                callback.accept(true);
            } catch (Exception e) {
                Log.error("FamilyService: failed to add member: " + e.getMessage());
                callback.accept(false);
            }
        });
    }

    /**
     * Remove a player from a family.
     */
    public static void removeMemberAsync(String familyId, String playerUuid, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.removeMember(familyId, playerUuid);
                playerFamily.remove(playerUuid);
                memberCache.remove(familyId);

                Family family = familyCache.get(familyId);
                if (family != null) family.setMemberCount(Math.max(0, family.getMemberCount() - 1));

                Log.info("FamilyService: " + playerUuid + " left family " + familyId);
                callback.run();
            } catch (Exception e) {
                Log.error("FamilyService: failed to remove member: " + e.getMessage());
            }
        });
    }

    /**
     * Update a member's role (e.g. promote to CO_OWNER).
     */
    public static void updateMemberRoleAsync(String familyId, String playerUuid,
                                             FamilyRole role, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.updateMemberRole(familyId, playerUuid, role);
                memberCache.remove(familyId); // force reload
                callback.run();
            } catch (Exception e) {
                Log.error("FamilyService: failed to update member role: " + e.getMessage());
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HOME
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load the family home asynchronously.
     * Callback receives null if no home is set.
     */
    public static void loadHomeAsync(String familyId, Consumer<FamilyHome> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                FamilyHome home = repository.getHome(familyId);
                callback.accept(home);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load home for " + familyId + ": " + e.getMessage());
                callback.accept(null);
            }
        });
    }

    /**
     * Set the family home location.
     */
    public static void setHomeAsync(String familyId, String world,
                                    double x, double y, double z,
                                    float yaw, float pitch,
                                    String setByUuid, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                repository.setHome(familyId, world, x, y, z, yaw, pitch, setByUuid);
                callback.run();
            } catch (Exception e) {
                Log.error("FamilyService: failed to set home for " + familyId + ": " + e.getMessage());
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BUFFS
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Load all currently active buffs for a family.
     */
    public static void loadActiveBuffsAsync(String familyId, Consumer<List<FamilyActiveBuff>> callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                List<FamilyActiveBuff> buffs = repository.getActiveBuffs(familyId);
                callback.accept(buffs);
            } catch (Exception e) {
                Log.error("FamilyService: failed to load buffs for " + familyId + ": " + e.getMessage());
                callback.accept(List.of());
            }
        });
    }

    /**
     * Activate a buff for a family.
     * Duration is calculated from the buff's base hours × level.
     */
    public static void activateBuffAsync(String familyId, FamilyBuff buffType,
                                         int level, String activatedByUuid,
                                         Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                int hours = buffType.getEffectiveDurationHours(level);
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(hours);

                repository.activateBuff(familyId, buffType, level, expiresAt, activatedByUuid);
                Log.info("FamilyService: buff " + buffType.name() + " lvl " + level
                        + " activated for family " + familyId + " (expires in " + hours + "h)");
                callback.run();
            } catch (Exception e) {
                Log.error("FamilyService: failed to activate buff: " + e.getMessage());
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CACHE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Evict all cache entries for a player on logout.
     * Does NOT evict the family itself – other members may still need it.
     */
    public static void invalidatePlayer(String playerUuid) {
        playerFamily.remove(playerUuid);
    }

    /**
     * Full cache reset on plugin disable.
     */
    public static void clearAll() {
        familyCache.clear();
        memberCache.clear();
        playerFamily.clear();
    }
}