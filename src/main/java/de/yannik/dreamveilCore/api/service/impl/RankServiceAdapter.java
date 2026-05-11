package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.IRankService;
import de.yannik.dreamveilCore.rank.model.PlayerRank;
import de.yannik.dreamveilCore.rank.service.RankService;
import java.util.function.Consumer;

/**
 * Adapter for public rank API
 */
public class RankServiceAdapter implements IRankService {

    @Override
    public PlayerRank getDisplayRank(String uuid) {
        return RankService.getDisplayRank(uuid);
    }

    @Override
    public String getDisplayName(String uuid) {
        return RankService.getDisplayName(uuid);
    }

    @Override
    public boolean hasPermission(String uuid, String permission) {
        return RankService.hasPermission(uuid, permission);
    }

    @Override
    public String[] getAllPermissions(String uuid) {
        return RankService.getAllPermissions(uuid);
    }

    @Override
    public void loadRanksAsync(String uuid, Consumer<Object> callback) {
        RankService.loadRanksAsync(uuid, callback::accept);
    }

    @Override
    public PlayerRank[] getAllRanks() {
        return PlayerRank.values();
    }

    @Override
    public boolean isRankPermanent(String uuid) {
        var ranks = RankService.getRanks(uuid);
        if (ranks == null) return false;
        return ranks.stream().anyMatch(r -> r.getExpiresAt() == null);
    }

    @Override
    public void addRankAsync(String uuid, String playerName, PlayerRank rank,
                             int durationDays, String grantedBy, Runnable callback) {
        RankService.addRankAsync(uuid, playerName, rank, durationDays, grantedBy, callback);
    }

    @Override
    public void removeRankAsync(String uuid, String playerName, PlayerRank rank,
                                String revokedBy, Runnable callback) {
        RankService.removeRankAsync(uuid, playerName, rank, revokedBy, callback);
    }

    @Override
    public void checkExpiredRanksAsync(String uuid, String playerName, Runnable callback) {
        RankService.checkExpiredRanksAsync(uuid, playerName, callback);
    }
}