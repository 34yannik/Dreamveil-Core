package de.yannik.dreamveilCore.api;

import de.yannik.dreamveilCore.api.service.IActivityService;
import de.yannik.dreamveilCore.api.service.IEconomyService;
import de.yannik.dreamveilCore.api.service.IPlayerService;
import de.yannik.dreamveilCore.api.service.IRankService;
import de.yannik.dreamveilCore.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Main API entry point for DreamveilCore.
 * External plugins use this class to access core services.
 *
 * Usage (from external plugin):
 * <pre>
 *     IEconomyService economy = DreamveilAPI.getEconomy();
 *     if (economy != null) {
 *         economy.addBalanceAsync(uuid, 100, () -> {
 *             // done
 *         });
 *     }
 * </pre>
 *
 * All service references are thread-safe.
 * Null checks are recommended for graceful degradation if DreamveilCore is disabled.
 */
public class DreamveilAPI {

    private static IPlayerService playerService;
    private static IEconomyService economyService;
    private static IActivityService activityService;
    private static IRankService rankService;

    private static final String PLUGIN_NAME = "DreamveilCore";

    // ==================== GETTER METHODS (Public) ====================

    /**
     * Get player service
     * Returns null if DreamveilCore is not loaded or disabled
     */
    public static IPlayerService getPlayer() {
        return playerService;
    }

    /**
     * Get economy service
     * Returns null if DreamveilCore is not loaded or disabled
     */
    public static IEconomyService getEconomy() {
        return economyService;
    }

    /**
     * Get activity service
     * Returns null if DreamveilCore is not loaded or disabled
     */
    public static IActivityService getActivity() {
        return activityService;
    }

    /**
     * Get rank service
     * Returns null if DreamveilCore is not loaded or disabled
     */
    public static IRankService getRank() {
        return rankService;
    }

    /**
     * Check if DreamveilCore is loaded and API is available
     */
    public static boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        return plugin != null && plugin.isEnabled() && playerService != null;
    }

    /**
     * Require service availability (convenience method)
     * Throws exception if API is not available
     */
    public static void requireAvailable() throws IllegalStateException {
        if (!isAvailable()) {
            throw new IllegalStateException("DreamveilCore API is not available. " +
                    "Make sure DreamveilCore is installed, enabled, and listed in plugin.yml as a dependency.");
        }
    }

    // ==================== SETTER METHODS (INTERNAL - called by DreamveilCore) ====================

    /**
     * Set player service (INTERNAL - called only by DreamveilCore)
     *
     * @param service The player service implementation
     */
    public static void setPlayerService(IPlayerService service) {
        playerService = service;
        Log.info("API: Player service registered");
    }

    /**
     * Set economy service (INTERNAL - called only by DreamveilCore)
     *
     * @param service The economy service implementation
     */
    public static void setEconomyService(IEconomyService service) {
        economyService = service;
        Log.info("API: Economy service registered");
    }

    /**
     * Set activity service (INTERNAL - called only by DreamveilCore)
     *
     * @param service The activity service implementation
     */
    public static void setActivityService(IActivityService service) {
        activityService = service;
        Log.info("API: Activity service registered");
    }

    /**
     * Set rank service (INTERNAL - called only by DreamveilCore)
     *
     * @param service The rank service implementation
     */
    public static void setRankService(IRankService service) {
        rankService = service;
        Log.info("API: Rank service registered");
    }

    /**
     * Reset all services (INTERNAL - called by DreamveilCore on disable)
     */
    public static void reset() {
        playerService = null;
        economyService = null;
        activityService = null;
        rankService = null;
        Log.info("API: Services reset");
    }
}