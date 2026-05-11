package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.IPlayerService;
import de.yannik.dreamveilCore.player.model.PlayerData;
import de.yannik.dreamveilCore.player.service.PlayerService;

import java.util.function.Consumer;

/**
 * Adapter that implements IPlayerService using the internal PlayerService.
 * This allows the API to expose only the interface, not the implementation.
 */
public class PlayerServiceAdapter implements IPlayerService {

    @Override
    public void loadPlayerAsync(String uuid, Consumer<PlayerData> callback) {
        PlayerService.loadPlayerAsync(uuid, callback);
    }

    @Override
    public PlayerData getPlayer(String uuid) {
        return PlayerService.getPlayer(uuid);
    }

    @Override
    public boolean isPlayerCached(String uuid) {
        return PlayerService.getPlayer(uuid) != null;
    }
}