package de.yannik.dreamveilCore.database.repository;

import de.yannik.dreamveilCore.database.Database;
import de.yannik.dreamveilCore.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Repository for economy-related persistence.
 * Handles balance and shards operations.
 */
public class EconomyRepository {

    /**
     * Update player balance
     */
    public void updateBalance(String uuid, long newBalance) throws SQLException {
        String query = "UPDATE player_economy SET balance = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, newBalance);
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        }
    }

    /**
     * Update player shards
     */
    public void updateShards(String uuid, long newShards) throws SQLException {
        String query = "UPDATE player_economy SET shards = ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, newShards);
            stmt.setString(2, uuid);
            stmt.executeUpdate();

        }
    }

    /**
     * Add balance to existing amount (atomic)
     */
    public void addBalance(String uuid, long amount) throws SQLException {
        String query = "UPDATE player_economy SET balance = balance + ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, amount);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    /**
     * Add shards to existing amount (atomic)
     */
    public void addShards(String uuid, long amount) throws SQLException {
        String query = "UPDATE player_economy SET shards = shards + ? WHERE uuid = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, amount);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }
}