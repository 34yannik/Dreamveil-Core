package de.yannik.dreamveilCore.rank.service;

import de.yannik.dreamveilCore.DreamveilCore;
import de.yannik.dreamveilCore.database.DatabaseExecutor;
import de.yannik.dreamveilCore.database.repository.RankLogRepository;
import de.yannik.dreamveilCore.database.repository.RankRepository;
import de.yannik.dreamveilCore.rank.cache.RankCache;
import de.yannik.dreamveilCore.rank.model.PlayerRank;
import de.yannik.dreamveilCore.rank.model.PlayerRankData;
import de.yannik.dreamveilCore.rank.util.GradientUtil;
import de.yannik.dreamveilCore.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service for rank operations
 * All operations are asynchronous
 */
public class RankService {

    private static final RankRepository rankRepository = new RankRepository();
    private static final RankLogRepository rankLogRepository = new RankLogRepository();

    /**
     * Get all active ranks for player (synchronous, from cache)
     */
    public static List<PlayerRankData> getRanks(String uuid) {
        return RankCache.getRanks(uuid);
    }

    /**
     * Get highest priority rank for player (synchronous, from cache)
     */
    public static PlayerRank getDisplayRank(String uuid) {
        PlayerRank rank = RankCache.getDisplayRank(uuid);
        return rank != null ? rank : PlayerRank.PLAYER;
    }

    /**
     * Get display name with gradient
     */
    public static String getDisplayName(String uuid) {
        PlayerRank rank = getDisplayRank(uuid);
        return GradientUtil.applyGradient(
                rank.getDisplayName(),
                rank.getColorStart(),
                rank.getColorEnd()
        );
    }

    /**
     * Load player ranks asynchronously
     */
    public static void loadRanksAsync(String uuid, Consumer<List<PlayerRankData>> callback) {
        List<PlayerRankData> cached = RankCache.getRanks(uuid);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        DatabaseExecutor.runAsync(() -> {
            try {
                List<PlayerRankData> ranks = rankRepository.getPlayerRanks(uuid);

                if (ranks.isEmpty()) {
                    ranks = List.of(new PlayerRankData(uuid, PlayerRank.PLAYER,
                            LocalDateTime.now(), null, "SYSTEM"));
                }

                // Cache and calculate display rank
                RankCache.putRanks(uuid, ranks);
                updateDisplayRank(uuid, ranks);

                callback.accept(ranks);
            } catch (Exception e) {
                Log.error("Failed to load ranks for " + uuid + ": " + e.getMessage());
                callback.accept(List.of());
            }
        });
    }

    /**
     * Add rank to player asynchronously
     */
    public static void addRankAsync(String uuid, String playerName, PlayerRank rank,
                                    int durationDays, String grantedBy, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                LocalDateTime expiresAt = durationDays > 0 ?
                        LocalDateTime.now().plusDays(durationDays) : null;

                rankRepository.addRank(uuid, rank, expiresAt, grantedBy);
                rankLogRepository.logRankAction(uuid, playerName, rank, "GRANT", expiresAt, grantedBy);

                // Reload ranks
                loadRanksAsync(uuid, ranks -> {
                    updateDisplayRank(uuid, ranks);

                    // Update permissions if online
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        updatePlayerPermissions(player, ranks);
                        player.sendMessage(DreamveilCore.DREAMVEIL_PREFIX + "§aYou got a new rank §7- " +
                                getDisplayName(uuid));
                    }

                    Log.info("Rank added: " + playerName + " -> " + rank.name());
                    callback.run();
                });
            } catch (Exception e) {
                Log.error("Failed to add rank for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Remove rank from player asynchronously
     */
    public static void removeRankAsync(String uuid, String playerName, PlayerRank rank,
                                       String revokedBy, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                rankRepository.removeRank(uuid, rank);
                rankLogRepository.logRankAction(uuid, playerName, rank, "REVOKE", null, revokedBy);

                // Reload ranks
                loadRanksAsync(uuid, ranks -> {
                    updateDisplayRank(uuid, ranks);

                    // Update permissions if online
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        updatePlayerPermissions(player, ranks);
                        player.sendMessage(DreamveilCore.DREAMVEIL_PREFIX + "§cYou lost your rank §7- " +
                                rank.getDisplayName());
                    }

                    Log.info("Rank removed: " + playerName + " <- " + rank.name());
                    callback.run();
                });
            } catch (Exception e) {
                Log.error("Failed to remove rank for " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Check and remove expired ranks
     */
    public static void checkExpiredRanksAsync(String uuid, String playerName, Runnable callback) {
        DatabaseExecutor.runAsync(() -> {
            try {
                List<PlayerRankData> expired = rankRepository.removeExpiredRanks(uuid);

                if (!expired.isEmpty()) {
                    for (PlayerRankData rankData : expired) {
                        rankLogRepository.logRankAction(uuid, playerName, rankData.getRank(),
                                "EXPIRE", rankData.getExpiresAt(), "SYSTEM");
                    }

                    // Reload ranks
                    loadRanksAsync(uuid, ranks -> {
                        updateDisplayRank(uuid, ranks);

                        // Update permissions if online
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            updatePlayerPermissions(player, ranks);

                            StringBuilder expiredRanks = new StringBuilder();
                            for (PlayerRankData rankData : expired) {
                                if (expiredRanks.length() > 0) expiredRanks.append(", ");
                                expiredRanks.append(rankData.getRank().getDisplayName());
                            }

                            player.sendMessage(DreamveilCore.DREAMVEIL_PREFIX + "§cYour rank has expired §7- " + expiredRanks);
                        }

                        Log.info("Removed " + expired.size() + " expired ranks for " + playerName);
                        callback.run();
                    });
                } else {
                    callback.run();
                }
            } catch (Exception e) {
                Log.error("Failed to check expired ranks for " + uuid + ": " + e.getMessage());
                callback.run();
            }
        });
    }

    /**
     * Check if player has permission
     */
    public static boolean hasPermission(String uuid, String permission) {
        List<PlayerRankData> ranks = RankCache.getRanks(uuid);
        if (ranks == null) return false;

        for (PlayerRankData rankData : ranks) {
            if (!rankData.isExpired()) {
                PlayerRank rank = rankData.getRank();
                for (String perm : rank.getPermissions()) {
                    if (permission.equals(perm) ||
                            perm.endsWith("*") && permission.startsWith(perm.substring(0, perm.length() - 1))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Get all permissions for player
     */
    public static String[] getAllPermissions(String uuid) {
        List<PlayerRankData> ranks = RankCache.getRanks(uuid);
        if (ranks == null) return new String[0];

        List<String> permissions = new ArrayList<>();
        for (PlayerRankData rankData : ranks) {
            if (!rankData.isExpired()) {
                for (String perm : rankData.getRank().getPermissions()) {
                    if (!permissions.contains(perm)) {
                        permissions.add(perm);
                    }
                }
            }
        }

        return permissions.toArray(new String[0]);
    }

    /**
     * Calculate display rank (highest priority)
     */
    private static void updateDisplayRank(String uuid, List<PlayerRankData> ranks) {
        PlayerRank highestRank = PlayerRank.PLAYER;

        for (PlayerRankData rankData : ranks) {
            if (!rankData.isExpired()) {
                if (rankData.getRank().getPriority() > highestRank.getPriority()) {
                    highestRank = rankData.getRank();
                }
            }
        }

        RankCache.putDisplayRank(uuid, highestRank);
    }

    /**
     * Update player permissions in-game
     */
    private static void updatePlayerPermissions(Player player, List<PlayerRankData> ranks) {
        // Just logging for now
        String[] perms = getAllPermissions(player.getUniqueId().toString());
    }
}