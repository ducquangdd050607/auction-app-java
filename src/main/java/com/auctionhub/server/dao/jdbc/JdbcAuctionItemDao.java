package com.auctionhub.server.dao.jdbc;

import com.auctionhub.common.enums.ItemType;
import com.auctionhub.common.factory.AuctionItemFactory;
import com.auctionhub.common.model.AuctionItem;
import com.auctionhub.server.dao.AuctionItemDao;
import com.auctionhub.server.db.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class JdbcAuctionItemDao implements AuctionItemDao {
    private final DatabaseManager databaseManager;

    public JdbcAuctionItemDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public AuctionItem save(AuctionItem item) {
        String sql = """
                INSERT INTO auction_items (id, seller_id, title, description, starting_price, item_type, attribute_one, attribute_two, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        executeUpsert(sql, item);
        return item;
    }

    @Override
    public AuctionItem update(AuctionItem item) {
        String sql = """
                UPDATE auction_items
                SET title = ?, description = ?, starting_price = ?, item_type = ?, attribute_one = ?, attribute_two = ?, updated_at = ?
                WHERE id = ?
                """;
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getTitle());
            statement.setString(2, item.getDescription());
            statement.setBigDecimal(3, item.getStartingPrice());
            statement.setString(4, item.getItemType().name());
            statement.setString(5, item.getAttributeOne());
            statement.setString(6, item.getAttributeTwo());
            statement.setObject(7, item.getUpdatedAt());
            statement.setString(8, item.getId().toString());
            statement.executeUpdate();
            return item;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể cập nhật item", ex);
        }
    }

    @Override
    public Optional<AuctionItem> findById(UUID id) {
        String sql = "SELECT * FROM auction_items WHERE id = ?";
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tìm item", ex);
        }
    }

    @Override
    public void deleteById(UUID id) {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM auction_items WHERE id = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể xoá item", ex);
        }
    }

    @Override
    public long count() {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM auction_items");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đếm item", ex);
        }
    }

    private void executeUpsert(String sql, AuctionItem item) {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getId().toString());
            statement.setString(2, item.getSellerId().toString());
            statement.setString(3, item.getTitle());
            statement.setString(4, item.getDescription());
            statement.setBigDecimal(5, item.getStartingPrice());
            statement.setString(6, item.getItemType().name());
            statement.setString(7, item.getAttributeOne());
            statement.setString(8, item.getAttributeTwo());
            statement.setObject(9, item.getCreatedAt());
            statement.setObject(10, item.getUpdatedAt());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể lưu item", ex);
        }
    }

    private AuctionItem mapRow(ResultSet resultSet) throws SQLException {
        return AuctionItemFactory.create(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                UUID.fromString(resultSet.getString("seller_id")),
                ItemType.valueOf(resultSet.getString("item_type")),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("starting_price"),
                resultSet.getString("attribute_one"),
                resultSet.getString("attribute_two"));
    }
}
