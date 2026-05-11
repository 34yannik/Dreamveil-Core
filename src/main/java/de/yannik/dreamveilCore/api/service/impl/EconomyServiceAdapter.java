package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.IEconomyService;
import de.yannik.dreamveilCore.player.service.EconomyService;

/**
 * Adapter that implements IEconomyService using the internal EconomyService.
 * This allows the API to expose only the interface, not the implementation.
 */
public class EconomyServiceAdapter implements IEconomyService {

    @Override
    public long getBalance(String uuid) {
        return EconomyService.getBalance(uuid);
    }

    @Override
    public long getShards(String uuid) {
        return EconomyService.getShards(uuid);
    }

    @Override
    public void addBalanceAsync(String uuid, long amount, Runnable callback) {
        EconomyService.addBalanceAsync(uuid, amount, callback);
    }

    @Override
    public void setBalanceAsync(String uuid, long amount, Runnable callback) {
        EconomyService.setBalanceAsync(uuid, amount, callback);
    }

    @Override
    public void addShardsAsync(String uuid, long amount, Runnable callback) {
        EconomyService.addShardsAsync(uuid, amount, callback);
    }

    @Override
    public void setShardsAsync(String uuid, long amount, Runnable callback) {
        EconomyService.setShardsAsync(uuid, amount, callback);
    }
}