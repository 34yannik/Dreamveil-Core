package de.yannik.dreamveilCore.api;

import de.yannik.dreamveilCore.api.service.*;
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
 *         economy.addBalanceAsync(uuid, 100, () -> {});
 *     }
 *
 *     IColorService colors = DreamveilAPI.getColors();
 *     if (colors != null) {
 *         colors.unlockColorAsync(uuid, PlayerColor.OCEAN, () -> {});
 *     }
 *
 *     IFamilyService families = DreamveilAPI.getFamilies();
 *     if (families != null) {
 *         families.loadFamilyByPlayerAsync(uuid, family -> { ... });
 *     }
 * </pre>
 *
 * All service references are thread-safe.
 * Null checks are recommended for graceful degradation if DreamveilCore is disabled.
 */
public class DreamveilAPI {

    private static IPlayerService   playerService;
    private static IEconomyService  economyService;
    private static IActivityService activityService;
    private static IRankService     rankService;
    private static ISettingsService settingsService;
    private static ITitleService    titleService;
    private static IColorService    colorService;
    private static IFamilyService   familyService;

    private static final String PLUGIN_NAME = "DreamveilCore";

    // ── Getters (Public) ──────────────────────────────────────────────────────

    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static IPlayerService   getPlayer()   { return playerService;   }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static IEconomyService  getEconomy()  { return economyService;  }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static IActivityService getActivity() { return activityService; }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static IRankService     getRank()     { return rankService;     }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static ISettingsService getSettings() { return settingsService; }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static ITitleService    getTitles()   { return titleService;    }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static IColorService    getColors()   { return colorService;    }
    /** Returns null if DreamveilCore is not loaded or disabled. */
    public static IFamilyService   getFamilies() { return familyService;   }

    // ── Availability ──────────────────────────────────────────────────────────

    /** Check if DreamveilCore is loaded and API is available. */
    public static boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        return plugin != null && plugin.isEnabled() && playerService != null;
    }

    /** Throws {@link IllegalStateException} if the API is not available. */
    public static void requireAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "DreamveilCore API is not available. " +
                    "Make sure DreamveilCore is installed, enabled, and listed " +
                    "in plugin.yml as a dependency.");
        }
    }

    // ── Setters (Internal) ────────────────────────────────────────────────────

    public static void setPlayerService(IPlayerService service) {
        playerService = service;
        Log.info("API: Player service registered");
    }

    public static void setEconomyService(IEconomyService service) {
        economyService = service;
        Log.info("API: Economy service registered");
    }

    public static void setActivityService(IActivityService service) {
        activityService = service;
        Log.info("API: Activity service registered");
    }

    public static void setRankService(IRankService service) {
        rankService = service;
        Log.info("API: Rank service registered");
    }

    public static void setSettingsService(ISettingsService service) {
        settingsService = service;
        Log.info("API: Settings service registered");
    }

    public static void setTitleService(ITitleService service) {
        titleService = service;
        Log.info("API: Title service registered");
    }

    public static void setColorService(IColorService service) {
        colorService = service;
        Log.info("API: Color service registered");
    }

    public static void setFamilyService(IFamilyService service) {
        familyService = service;
        Log.info("API: Family service registered");
    }

    /**
     * Reset all services (INTERNAL – called by DreamveilCore on disable).
     */
    public static void reset() {
        playerService   = null;
        economyService  = null;
        activityService = null;
        rankService     = null;
        settingsService = null;
        titleService    = null;
        colorService    = null;
        familyService   = null;
        Log.info("API: All services reset");
    }
}