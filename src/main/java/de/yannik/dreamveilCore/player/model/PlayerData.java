package de.yannik.dreamveilCore.player.model;

import java.time.LocalDateTime;

/**
 * Core player data model - represents a player's complete profile
 * across all domains (core, economy, activity).
 * Mutable to support updates before persistence.
 */
public class PlayerData {

    // Core
    private String uuid;
    private String username;
    private LocalDateTime firstLogin;
    private LocalDateTime lastLogin;
    private LocalDateTime lastLogout;

    // Economy
    private long balance;
    private long shards;

    // Activity
    private long sessionStart;
    private long playtime;
    private int loginStreak;
    private int longestStreak;

    public PlayerData(String uuid) {
        this.uuid = uuid;
        this.balance = 250;
        this.shards = 0;
        this.playtime = 0;
        this.loginStreak = 0;
        this.longestStreak = 0;
    }

    // Constructor for full initialization
    public PlayerData(String uuid, String username, LocalDateTime firstLogin,
                      LocalDateTime lastLogin, LocalDateTime lastLogout,
                      long balance, long shards, long playtime,
                      int loginStreak, int longestStreak) {
        this.uuid = uuid;
        this.username = username;
        this.firstLogin = firstLogin;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.balance = balance;
        this.shards = shards;
        this.playtime = playtime;
        this.loginStreak = loginStreak;
        this.longestStreak = longestStreak;
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(LocalDateTime firstLogin) {
        this.firstLogin = firstLogin;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(LocalDateTime lastLogout) {
        this.lastLogout = lastLogout;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getShards() {
        return shards;
    }

    public void setShards(long shards) {
        this.shards = shards;
    }

    public long getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(long sessionStart) {
        this.sessionStart = sessionStart;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public int getLoginStreak() {
        return loginStreak;
    }

    public void setLoginStreak(int loginStreak) {
        this.loginStreak = loginStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", shards=" + shards +
                ", playtime=" + playtime +
                '}';
    }
}