package de.yannik.dreamveilCore.player.cache;

import de.yannik.dreamveilCore.player.model.PlayerSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsCache {

    private static final Map<String, PlayerSettings> cache = new ConcurrentHashMap<>();

    public static void put(String uuid, PlayerSettings settings) {
        cache.put(uuid, settings);
    }

    public static PlayerSettings get(String uuid) {
        return cache.get(uuid);
    }

    public static void remove(String uuid) {
        cache.remove(uuid);
    }

    public static boolean contains(String uuid) {
        return cache.containsKey(uuid);
    }
}