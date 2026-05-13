package de.yannik.dreamveilCore;

import de.yannik.dreamveilCore.api.DreamveilAPI;
import de.yannik.dreamveilCore.api.service.IActivityService;
import de.yannik.dreamveilCore.api.service.IEconomyService;
import de.yannik.dreamveilCore.api.service.IPlayerService;
import de.yannik.dreamveilCore.api.service.IRankService;
import de.yannik.dreamveilCore.api.service.impl.ActivityServiceAdapter;
import de.yannik.dreamveilCore.api.service.impl.EconomyServiceAdapter;
import de.yannik.dreamveilCore.api.service.impl.PlayerServiceAdapter;
import de.yannik.dreamveilCore.api.service.impl.RankServiceAdapter;
import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.listener.PlayerEventHandler;
import de.yannik.dreamveilCore.listener.RankEventHandler;
import de.yannik.dreamveilCore.player.task.PlaytimeTask;
import de.yannik.dreamveilCore.rank.util.GradientUtil;
import de.yannik.dreamveilCore.util.Log;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class DreamveilCore extends JavaPlugin {

    public static String DREAMVEIL_PREFIX = GradientUtil.applyGradient(
            "Dreamveil",
            ChatColor.of("#C2185B"),
            ChatColor.of("#FFB6C1")
    ) + " §8- §7";

    private Database database;
    private PlaytimeTask playtimeTask;

    @Override
    public void onEnable() {
        Log.init(this);

        // Create default config
        saveDefaultConfig();

        try {
            // Initialize database
            database = new Database(this);
            database.initializeDatabase();
            Log.info("Database initialized");

            // Register event listeners
            Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(playtimeTask), this);
            getServer().getPluginManager().registerEvents(new RankEventHandler(), this);
            Log.info("Event listeners registered");

            // Initialize and register API services
            initializeAPI();
            Log.info("API services registered");

            // Start bukkit tasks
            playtimeTask = new PlaytimeTask(this);
            playtimeTask.start();
            Log.info("Tasks started");

            Log.info("DreamveilCore enabled successfully!");
        } catch (Exception e) {
            Log.error("Failed to enable plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Initialize API services and register them with DreamveilAPI
     */
    private void initializeAPI() {
        // Create service adapters
        IPlayerService playerService = new PlayerServiceAdapter();
        IEconomyService economyService = new EconomyServiceAdapter();
        IActivityService activityService = new ActivityServiceAdapter();
        IRankService rankService = new RankServiceAdapter();

        // Register with API
        DreamveilAPI.setPlayerService(playerService);
        DreamveilAPI.setEconomyService(economyService);
        DreamveilAPI.setActivityService(activityService);
        DreamveilAPI.setRankService(rankService);

        // Also register with Bukkit ServicesManager for plugin discovery
        registerBukkitServices(playerService, economyService, activityService, rankService);
    }

    /**
     * Register services with Bukkit's ServicesManager for discovery by other plugins
     */
    private void registerBukkitServices(IPlayerService playerService,
                                        IEconomyService economyService,
                                        IActivityService activityService,
                                        IRankService rankService) {
        try {
            getServer().getServicesManager().register(
                    IPlayerService.class,
                    playerService,
                    this,
                    ServicePriority.Normal
            );

            getServer().getServicesManager().register(
                    IEconomyService.class,
                    economyService,
                    this,
                    ServicePriority.Normal
            );

            getServer().getServicesManager().register(
                    IActivityService.class,
                    activityService,
                    this,
                    ServicePriority.Normal
            );

            getServer().getServicesManager().register(
                    IRankService.class,
                    rankService,
                    this,
                    ServicePriority.Normal
            );
            Log.info("Services registered with Bukkit ServicesManager");
        } catch (Exception e) {
            Log.error("Failed to register services with ServicesManager: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Stop Bukkit Tasks
        playtimeTask.stop();

        // Reset API
        DreamveilAPI.reset();

        // Shutdown async executor
        DatabaseExecutor.shutdown();

        // Close database connections
        Database.closeDataSource();


        Log.info("DreamveilCore disabled");
    }
}
