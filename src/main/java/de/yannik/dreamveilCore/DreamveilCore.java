package de.yannik.dreamveilCore;

import de.yannik.dreamveilCore.api.DreamveilAPI;
import de.yannik.dreamveilCore.api.service.*;
import de.yannik.dreamveilCore.api.service.impl.*;
import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.family.service.FamilyService;
import de.yannik.dreamveilCore.listener.PlayerEventHandler;
import de.yannik.dreamveilCore.listener.RankEventHandler;
import de.yannik.dreamveilCore.player.task.PlaytimeTask;
import de.yannik.dreamveilCore.util.GradientUtil;
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

    private Database     database;
    private PlaytimeTask playtimeTask;

    @Override
    public void onEnable() {
        Log.init(this);
        saveDefaultConfig();

        try {
            // Database
            database = new Database(this);
            database.initializeDatabase();
            Log.info("Database initialized");

            // Listeners
            Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(playtimeTask, this), this);
            getServer().getPluginManager().registerEvents(new RankEventHandler(), this);
            Log.info("Event listeners registered");

            // API
            initializeAPI();
            Log.info("API services registered");

            // Tasks
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

    private void initializeAPI() {
        IPlayerService   playerService   = new PlayerServiceAdapter();
        IEconomyService  economyService  = new EconomyServiceAdapter();
        IActivityService activityService = new ActivityServiceAdapter();
        IRankService     rankService     = new RankServiceAdapter();
        ISettingsService settingsService = new SettingsServiceAdapter();
        ITitleService    titleService    = new TitleServiceAdapter();
        IColorService    colorService    = new ColorServiceAdapter();
        IFamilyService   familyService   = new FamilyServiceAdapter();

        DreamveilAPI.setPlayerService(playerService);
        DreamveilAPI.setEconomyService(economyService);
        DreamveilAPI.setActivityService(activityService);
        DreamveilAPI.setRankService(rankService);
        DreamveilAPI.setSettingsService(settingsService);
        DreamveilAPI.setTitleService(titleService);
        DreamveilAPI.setColorService(colorService);
        DreamveilAPI.setFamilyService(familyService);

        registerBukkitServices(
                playerService, economyService, activityService,
                rankService, settingsService, titleService,
                colorService, familyService
        );
    }

    private void registerBukkitServices(IPlayerService   playerService,
                                        IEconomyService  economyService,
                                        IActivityService activityService,
                                        IRankService     rankService,
                                        ISettingsService settingsService,
                                        ITitleService    titleService,
                                        IColorService    colorService,
                                        IFamilyService   familyService) {
        try {
            var sm = getServer().getServicesManager();
            sm.register(IPlayerService.class,   playerService,   this, ServicePriority.Normal);
            sm.register(IEconomyService.class,  economyService,  this, ServicePriority.Normal);
            sm.register(IActivityService.class, activityService, this, ServicePriority.Normal);
            sm.register(IRankService.class,     rankService,     this, ServicePriority.Normal);
            sm.register(ISettingsService.class, settingsService, this, ServicePriority.Normal);
            sm.register(ITitleService.class,    titleService,    this, ServicePriority.Normal);
            sm.register(IColorService.class,    colorService,    this, ServicePriority.Normal);
            sm.register(IFamilyService.class,   familyService,   this, ServicePriority.Normal);
            Log.info("Services registered with Bukkit ServicesManager");
        } catch (Exception e) {
            Log.error("Failed to register services with ServicesManager: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (playtimeTask != null) playtimeTask.stop();

        FamilyService.clearAll();

        DreamveilAPI.reset();
        DatabaseExecutor.shutdown();
        Database.closeDataSource();

        Log.info("DreamveilCore disabled");
    }
}