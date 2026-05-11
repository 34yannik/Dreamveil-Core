package de.yannik.dreamveilCore.rank.model;

import java.time.LocalDateTime;

/**
 * Represents a player's rank assignment
 */
public class PlayerRankData {

    private String uuid;
    private PlayerRank rank;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // null = permanent
    private String grantedBy; // "ADMIN", "SYSTEM", etc

    public PlayerRankData(String uuid, PlayerRank rank, LocalDateTime createdAt, 
                         LocalDateTime expiresAt, String grantedBy) {
        this.uuid = uuid;
        this.rank = rank;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.grantedBy = grantedBy;
    }

    public String getUuid() {
        return uuid;
    }

    public PlayerRank getRank() {
        return rank;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    /**
     * Check if rank has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Get duration in days (if set)
     */
    public long getDurationDays() {
        if (expiresAt == null) return -1; // Permanent
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, expiresAt);
    }

    @Override
    public String toString() {
        return "PlayerRankData{" +
                "uuid='" + uuid + '\'' +
                ", rank=" + rank +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", grantedBy='" + grantedBy + '\'' +
                '}';
    }
}