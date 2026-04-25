package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.dao.AutoBidDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcAutoBidDao extends JdbcDaoSupport implements AutoBidDao {
    @Override
    public AutoBidConfig save(AutoBidConfig config) {
        String sql = """
                INSERT INTO auto_bid_configs (
                    id, auction_id, bidder_id, max_bid, increment_amount, enabled, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    max_bid = VALUES(max_bid), increment_amount = VALUES(increment_amount),
                    enabled = VALUES(enabled), updated_at = VALUES(updated_at)
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(config.getId()));
            statement.setString(2, uuid(config.getAuctionId()));
            statement.setString(3, uuid(config.getBidderId()));
            statement.setBigDecimal(4, config.getMaxBid());
            statement.setBigDecimal(5, config.getIncrementAmount());
            statement.setBoolean(6, config.isEnabled());
            statement.setTimestamp(7, timestamp(config.getCreatedAt()));
            statement.setTimestamp(8, timestamp(config.getUpdatedAt()));
            statement.executeUpdate();
            return config;
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong luu duoc auto-bid", exception);
        }
    }

    @Override
    public Optional<AutoBidConfig> findByAuctionIdAndBidderId(UUID auctionId, UUID bidderId) {
        String sql = "SELECT * FROM auto_bid_configs WHERE auction_id = ? AND bidder_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(auctionId));
            statement.setString(2, uuid(bidderId));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapConfig(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc auto-bid", exception);
        }
    }

    @Override
    public List<AutoBidConfig> findEnabledByAuctionId(UUID auctionId) {
        String sql = "SELECT * FROM auto_bid_configs WHERE auction_id = ? AND enabled = TRUE ORDER BY created_at";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(auctionId));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AutoBidConfig> configs = new ArrayList<>();
                while (resultSet.next()) {
                    configs.add(mapConfig(resultSet));
                }
                return configs;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc danh sach auto-bid", exception);
        }
    }

    @Override
    public void deleteByAuctionId(UUID auctionId) {
        String sql = "DELETE FROM auto_bid_configs WHERE auction_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(auctionId));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong xoa duoc auto-bid", exception);
        }
    }

    private AutoBidConfig mapConfig(ResultSet resultSet) throws SQLException {
        return new AutoBidConfig(
                uuid(resultSet.getString("id")),
                localDateTime(resultSet.getTimestamp("created_at")),
                localDateTime(resultSet.getTimestamp("updated_at")),
                uuid(resultSet.getString("auction_id")),
                uuid(resultSet.getString("bidder_id")),
                resultSet.getBigDecimal("max_bid"),
                resultSet.getBigDecimal("increment_amount"),
                resultSet.getBoolean("enabled"));
    }
}
