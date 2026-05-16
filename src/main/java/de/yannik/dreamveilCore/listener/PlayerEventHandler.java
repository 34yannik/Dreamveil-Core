package de.yannik.dreamveilCore.listener;

import de.yannik.dreamveilCore.player.service.ActivityService;
import de.yannik.dreamveilCore.player.service.PlayerService;
import de.yannik.dreamveilCore.player.task.PlaytimeTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for player join/quit events.
 * Coordinates between Bukkit events and the service layer.
 */
public class PlayerEventHandler implements Listener {

    private final PlaytimeTask playtimeTask;

    public PlayerEventHandler(PlaytimeTask playtimeTask) {
        this.playtimeTask = playtimeTask;
    }

    /**
     * Handle player join
     * - Load player data asynchronously
     * - Increment login streak
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        event.setJoinMessage(null);

        // Load player data asynchronously (or create if new)
        PlayerService.getOrLoadAsync(uuid, playerData -> {
            if (playerData == null) {
                // New player - create profile
                PlayerService.createPlayerAsync(uuid, player.getName(), () -> {
                    // Callback: profile created
                });
            } else {
                // Existing player - increment login streak
                int currentStreak = ActivityService.getLoginStreak(uuid);
                ActivityService.updateLoginStreakAsync(uuid, currentStreak + 1, () -> {
                    // Callback: streak updated
                });
            }
        });

    }

    /**
     * Handle player quit
     * - Save player data asynchronously
     * - Remove from cache after save completes
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        event.setQuitMessage(null);

        playtimeTask.onPlayerQuit(uuid);

        // Update last logout and save data asynchronously
        PlayerService.updateLastLogoutAsync(uuid, () -> {
            // After save completes, remove from cache
            PlayerService.unloadPlayer(uuid);
        });

    }
}