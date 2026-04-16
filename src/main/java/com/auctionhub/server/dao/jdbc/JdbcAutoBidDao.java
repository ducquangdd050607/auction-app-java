package com.auctionhub.server.dao.jdbc;

import com.auctionhub.common.model.AutoBidConfig;
import com.auctionhub.server.dao.AutoBidDao;
import com.auctionhub.server.db.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcAutoBidDao implements AutoBidDao {
    private final DatabaseManager databaseManager;

    public JdbcAutoBidDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public AutoBidConfig upsert(AutoBidConfig config) {
        Optional<AutoBidConfig> existing = findByAuctionAndBidder(config.getAuctionId(), config.getBidderId());
        if (existing.isPresent()) {
            String sql = """
                    UPDATE auto_bid_configs
                    SET max_bid = ?, increment_amount = ?, enabled = ?, updated_at = ?
                    WHERE auction_id = ? AND bidder_id = ?
                    """;
            try (var connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBigDecimal(1, config.getMaxBid());
                statement.setBigDecimal(2, config.getIncrementAmount());
                statement.setBoolean(3, config.isEnabled());
                statement.setObject(4, config.getUpdatedAt());
                statement.setString(5, config.getAuctionId().toString());
                statement.setString(6, config.getBidderId().toString());
                statement.executeUpdate();
                return config;
            } catch (SQLException ex) {
                throw new IllegalStateException("Không thể cập nhật auto bid", ex);
            }
        }

        String sql = """
                INSERT INTO auto_bid_configs (id, auction_id, bidder_id, max_bid, increment_amount, enabled, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, config.getId().toString());
            statement.setString(2, config.getAuctionId().toString());
            statement.setString(3, config.getBidderId().toString());
            statement.setBigDecimal(4, config.getMaxBid());
            statement.setBigDecimal(5, config.getIncrementAmount());
            statement.setBoolean(6, config.isEnabled());
            statement.setObject(7, config.getCreatedAt());
            statement.setObject(8, config.getUpdatedAt());
            statement.executeUpdate();
            return config;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể lưu auto bid", ex);
        }
    }

    @Override
    public Optional<AutoBidConfig> findByAuctionAndBidder(UUID auctionId, UUID bidderId) {
        String sql = "SELECT * FROM auto_bid_configs WHERE auction_id = ? AND bidder_id = ?";
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, auctionId.toString());
            statement.setString(2, bidderId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tìm auto bid", ex);
        }
    }

    @Override
    public List<AutoBidConfig> findByAuctionId(UUID auctionId) {
        String sql = "SELECT * FROM auto_bid_configs WHERE auction_id = ? AND enabled = TRUE ORDER BY created_at ASC";
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, auctionId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AutoBidConfig> configs = new ArrayList<>();
                while (resultSet.next()) {
                    configs.add(mapRow(resultSet));
                }
                return configs;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể truy vấn auto bid", ex);
        }
    }

    @Override
    public void disableByAuctionId(UUID auctionId) {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE auto_bid_configs SET enabled = FALSE WHERE auction_id = ?")) {
            statement.setString(1, auctionId.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể vô hiệu auto bid", ex);
        }
    }

    private AutoBidConfig mapRow(ResultSet resultSet) throws SQLException {
        return new AutoBidConfig(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                UUID.fromString(resultSet.getString("auction_id")),
                UUID.fromString(resultSet.getString("bidder_id")),
                resultSet.getBigDecimal("max_bid"),
                resultSet.getBigDecimal("increment_amount"),
                resultSet.getBoolean("enabled")
        );
    }
}
