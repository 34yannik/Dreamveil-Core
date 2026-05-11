package de.yannik.dreamveilCore.player.task;

import de.yannik.dreamveilCore.DreamveilCore;
import de.yannik.dreamveilCore.player.service.ActivityService;
import de.yannik.dreamveilCore.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks and persists player playtime efficiently.
 * Batches saves to avoid overwhelming the database.
 */
public class PlaytimeTask {

    private final DreamveilCore plugin;

    // Sekunden pro Spieler-Session
    private final Map<String, Integer> sessionSeconds = new HashMap<>();

    // Konfigurierbare Parameter
    private static final int SAVE_INTERVAL_SECONDS = 300; // 5 Minuten
    private static final int BATCH_SIZE = 10; // Max 10 Queries gleichzeitig
    private static final int TICK_RATE = 20; // 1 Sekunde = 20 Ticks

    public PlaytimeTask(DreamveilCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Task 1: Zähle Playtime jede Sekunde
        new BukkitRunnable() {
            @Override
            public void run() {
                countPlaytime();
            }
        }.runTaskTimer(plugin, TICK_RATE, TICK_RATE);

        // Task 2: Speichere alle 5 Minuten (mit Batching)
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllBatched();
            }
        }.runTaskTimerAsynchronously(plugin, TICK_RATE * SAVE_INTERVAL_SECONDS,
                TICK_RATE * SAVE_INTERVAL_SECONDS);
    }

    /**
     * Count playtime for all online players (every second)
     */
    private void countPlaytime() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String uuid = player.getUniqueId().toString();
            sessionSeconds.put(uuid, sessionSeconds.getOrDefault(uuid, 0) + 1);
        }
    }

    /**
     * Save all playtime with batching to avoid DB overload
     */
    private void saveAllBatched() {
        if (sessionSeconds.isEmpty()) {
            Log.info("No playtime to save");
            return;
        }

        int totalEntries = sessionSeconds.size();
        Log.info("Saving playtime for " + totalEntries + " players (batched)");

        // Batch-weise speichern
        AtomicInteger batchCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (Map.Entry<String, Integer> entry : new HashMap<>(sessionSeconds).entrySet()) {
            String uuid = entry.getKey();
            int seconds = entry.getValue();

            // Skip invalid entries
            if (seconds <= 0) {
                sessionSeconds.remove(uuid);
                continue;
            }

            // Wait if batch size exceeded
            if (batchCount.get() >= BATCH_SIZE) {
                try {
                    Thread.sleep(100); // 100ms delay zwischen Batches
                    batchCount.set(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.error("Playtime save interrupted: " + e.getMessage());
                    break;
                }
            }

            long playtimeMs = (long) seconds * 1000;

            // Save asynchronously
            ActivityService.addPlaytimeAsync(uuid, playtimeMs, () -> {
                successCount.incrementAndGet();
            });

            batchCount.incrementAndGet();
        }

        // Clear after all saves initiated
        int savedCount = successCount.get();
        sessionSeconds.clear();

        Log.info("✓ Playtime save completed - Saved: " + savedCount +
                " / Total: " + totalEntries);
    }

    /**
     * Clean up player session on logout
     * Sollte von PlayerEventHandler aufgerufen werden
     */
    public void onPlayerQuit(String uuid) {
        int seconds = sessionSeconds.getOrDefault(uuid, 0);

        if (seconds > 0) {

            long playtimeMs = (long) seconds * 1000;
            ActivityService.addPlaytimeAsync(uuid, playtimeMs, () -> {
            });
        }

        sessionSeconds.remove(uuid);
    }

    /**
     * Get current session playtime for a player (for testing/info)
     */
    public int getSessionSeconds(String uuid) {
        return sessionSeconds.getOrDefault(uuid, 0);
    }

    /**
     * Stop all tasks (called on plugin disable)
     */
    public void stop() {
        // Tasks werden automatisch gecancelt wenn Plugin disabled
        // Aber wir können noch einen Final Save machen
        Log.info("Saving final playtime on plugin disable...");
        saveAllBatched();
    }
}