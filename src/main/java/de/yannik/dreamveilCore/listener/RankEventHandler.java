package de.yannik.dreamveilCore.listener;

import de.yannik.dreamveilCore.rank.service.RankService;
import de.yannik.dreamveilCore.util.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for rank operations
 */
public class RankEventHandler implements Listener {

    /**
     * On player join: Load ranks and check for expired ones
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        Log.info("Loading ranks for: " + player.getName());

        // Load ranks asynchronously
        RankService.loadRanksAsync(uuid, ranks -> {
            Log.info("Ranks loaded for " + player.getName() + ": " + ranks.size());

            // Check for expired ranks
            RankService.checkExpiredRanksAsync(uuid, player.getName(), () -> {
                // Reload to get final rank
                RankService.loadRanksAsync(uuid, finalRanks -> {
                    String displayName = RankService.getDisplayName(uuid);
                    Log.info("Display rank set: " + player.getName() + " -> " + displayName);
                });
            });
        });
    }

    /**
     * On player quit: Clear cache
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Keep in cache for a bit in case of quick rejoin
        // Will be auto-evicted after 30 minutes anyway
        Log.info("Player quit: " + player.getName());
    }
}