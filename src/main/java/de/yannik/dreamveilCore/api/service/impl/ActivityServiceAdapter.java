package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.IActivityService;
import de.yannik.dreamveilCore.player.service.ActivityService;

/**
 * Adapter that implements IActivityService using the internal ActivityService.
 * This allows the API to expose only the interface, not the implementation.
 */
public class ActivityServiceAdapter implements IActivityService {

    @Override
    public long getPlaytime(String uuid) {
        return ActivityService.getPlaytime(uuid);
    }

    @Override
    public int getLoginStreak(String uuid) {
        return ActivityService.getLoginStreak(uuid);
    }

    @Override
    public int getLongestStreak(String uuid) {
        return ActivityService.getLongestStreak(uuid);
    }

    @Override
    public void addPlaytimeAsync(String uuid, long milliseconds, Runnable callback) {
        ActivityService.addPlaytimeAsync(uuid, milliseconds, callback);
    }

    @Override
    public void setPlaytimeAsync(String uuid, long milliseconds, Runnable callback) {
        ActivityService.setPlaytimeAsync(uuid, milliseconds, callback);
    }

    @Override
    public void updateLoginStreakAsync(String uuid, int newStreak, Runnable callback) {
        ActivityService.updateLoginStreakAsync(uuid, newStreak, callback);
    }
}