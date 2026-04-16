package com.auctionhub.server.dao.jdbc;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.Auction;
import com.auctionhub.server.dao.AuctionDao;
import com.auctionhub.server.db.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JdbcAuctionDao implements AuctionDao {
    private final DatabaseManager databaseManager;

    public JdbcAuctionDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Auction save(Auction auction) {
        String sql = """
                INSERT INTO auctions (id, item_id, seller_id, current_price, leading_bidder_id, start_time, end_time, status, minimum_increment, winner_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, auction, true);
            statement.executeUpdate();
            return auction;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể lưu auction", ex);
        }
    }

    @Override
    public Auction update(Auction auction) {
        String sql = """
                UPDATE auctions
                SET current_price = ?, leading_bidder_id = ?, start_time = ?, end_time = ?, status = ?, minimum_increment = ?, winner_id = ?, updated_at = ?
                WHERE id = ?
                """;
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, auction, false);
            statement.executeUpdate();
            return auction;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể cập nhật auction", ex);
        }
    }

    @Override
    public Optional<Auction> findById(UUID id) {
        return query("SELECT * FROM auctions WHERE id = ?", statement -> statement.setString(1, id.toString())).stream().findFirst();
    }

    @Override
    public List<Auction> findAll() {
        return query("SELECT * FROM auctions ORDER BY end_time ASC", null);
    }

    @Override
    public List<Auction> findBySellerId(UUID sellerId) {
        return query("SELECT * FROM auctions WHERE seller_id = ? ORDER BY created_at DESC", statement -> statement.setString(1, sellerId.toString()));
    }

    @Override
    public List<Auction> findByStatuses(Collection<AuctionStatus> statuses) {
        if (statuses.isEmpty()) {
            return List.of();
        }
        String placeholders = statuses.stream().map(status -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT * FROM auctions WHERE status IN (" + placeholders + ")";
        return query(sql, statement -> {
            int index = 1;
            for (AuctionStatus status : statuses) {
                statement.setString(index++, status.name());
            }
        });
    }

    @Override
    public void deleteById(UUID id) {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM auctions WHERE id = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể xoá auction", ex);
        }
    }

    @Override
    public long count() {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM auctions");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đếm auction", ex);
        }
    }

    private List<Auction> query(String sql, StatementConfigurer configurer) {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurer != null) {
                configurer.configure(statement);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Auction> auctions = new ArrayList<>();
                while (resultSet.next()) {
                    auctions.add(mapRow(resultSet));
                }
                return auctions;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể truy vấn auction", ex);
        }
    }

    private Auction mapRow(ResultSet resultSet) throws SQLException {
        return new Auction(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                UUID.fromString(resultSet.getString("item_id")),
                UUID.fromString(resultSet.getString("seller_id")),
                resultSet.getBigDecimal("current_price"),
                resultSet.getString("leading_bidder_id") == null ? null : UUID.fromString(resultSet.getString("leading_bidder_id")),
                resultSet.getTimestamp("start_time").toLocalDateTime(),
                resultSet.getTimestamp("end_time").toLocalDateTime(),
                AuctionStatus.valueOf(resultSet.getString("status")),
                resultSet.getBigDecimal("minimum_increment"),
                resultSet.getString("winner_id") == null ? null : UUID.fromString(resultSet.getString("winner_id"))
        );
    }

    private void bind(PreparedStatement statement, Auction auction, boolean insert) throws SQLException {
        if (insert) {
            statement.setString(1, auction.getId().toString());
            statement.setString(2, auction.getItemId().toString());
            statement.setString(3, auction.getSellerId().toString());
            statement.setBigDecimal(4, auction.getCurrentPrice());
            statement.setString(5, auction.getLeadingBidderId() == null ? null : auction.getLeadingBidderId().toString());
            statement.setObject(6, auction.getStartTime());
            statement.setObject(7, auction.getEndTime());
            statement.setString(8, auction.getStatus().name());
            statement.setBigDecimal(9, auction.getMinimumIncrement());
            statement.setString(10, auction.getWinnerId() == null ? null : auction.getWinnerId().toString());
            statement.setObject(11, auction.getCreatedAt());
            statement.setObject(12, auction.getUpdatedAt());
        } else {
            statement.setBigDecimal(1, auction.getCurrentPrice());
            statement.setString(2, auction.getLeadingBidderId() == null ? null : auction.getLeadingBidderId().toString());
            statement.setObject(3, auction.getStartTime());
            statement.setObject(4, auction.getEndTime());
            statement.setString(5, auction.getStatus().name());
            statement.setBigDecimal(6, auction.getMinimumIncrement());
            statement.setString(7, auction.getWinnerId() == null ? null : auction.getWinnerId().toString());
            statement.setObject(8, auction.getUpdatedAt());
            statement.setString(9, auction.getId().toString());
        }
    }

    @FunctionalInterface
    private interface StatementConfigurer {
        void configure(PreparedStatement statement) throws SQLException;
    }
}
