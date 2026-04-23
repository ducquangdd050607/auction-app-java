package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.model.Item;
import com.auctionapp.auctionappjava.server.dao.AuctionItemDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.util.*;

public class JdbcAuctionItemDao implements AuctionItemDao {
    private final DatabaseManager databaseManager;
    public JdbcAuctionItemDao() { this(new DatabaseManager()); }
    public JdbcAuctionItemDao(DatabaseManager databaseManager) { this.databaseManager = databaseManager; }

    @Override public void save(Item item) {
        String sql = "INSERT INTO auction_items (id, seller_id, title, description, starting_price, item_type, attribute_one, attribute_two, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { bindFull(ps, item); ps.executeUpdate(); }
        catch (SQLException e) { throw new IllegalStateException("Không thể lưu item", e); }
    }

    @Override public void update(Item item) {
        item.touch();
        String sql = "UPDATE auction_items SET title=?, description=?, starting_price=?, item_type=?, attribute_one=?, attribute_two=?, updated_at=? WHERE id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getTitle()); ps.setString(2, item.getDescription()); ps.setBigDecimal(3, item.getStartingPrice()); ps.setString(4, item.getItemType().name());
            ps.setString(5, item.getAttributeOne()); ps.setString(6, item.getAttributeTwo()); ps.setObject(7, item.getUpdatedAt()); ps.setString(8, item.getId().toString()); ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Không thể cập nhật item", e); }
    }

    @Override public void delete(UUID itemId) { exec("DELETE FROM auction_items WHERE id=?", itemId.toString()); }
    @Override public Optional<Item> findById(UUID itemId) { List<Item> list = many("SELECT * FROM auction_items WHERE id=?", itemId.toString()); return list.isEmpty()?Optional.empty():Optional.of(list.get(0)); }
    @Override public List<Item> findBySellerId(UUID sellerId) { return many("SELECT * FROM auction_items WHERE seller_id=? ORDER BY created_at DESC", sellerId.toString()); }
    @Override public List<Item> findAll() { return many("SELECT * FROM auction_items ORDER BY created_at DESC"); }

    private void exec(String sql, Object... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { for (int i=0;i<params.length;i++) ps.setObject(i+1, params[i]); ps.executeUpdate(); }
        catch (SQLException e) { throw new IllegalStateException("Không thể thao tác item", e); }
    }

    private List<Item> many(String sql, Object... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i=0;i<params.length;i++) ps.setObject(i+1, params[i]); try (ResultSet rs = ps.executeQuery()) { List<Item> list = new ArrayList<>(); while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new IllegalStateException("Không thể tải item", e); }
    }

    private void bindFull(PreparedStatement ps, Item item) throws SQLException {
        ps.setString(1, item.getId().toString()); ps.setString(2, item.getSellerId().toString()); ps.setString(3, item.getTitle()); ps.setString(4, item.getDescription());
        ps.setBigDecimal(5, item.getStartingPrice()); ps.setString(6, item.getItemType().name()); ps.setString(7, item.getAttributeOne()); ps.setString(8, item.getAttributeTwo());
        ps.setObject(9, item.getCreatedAt()); ps.setObject(10, item.getUpdatedAt());
    }

    private Item map(ResultSet rs) throws SQLException {
        return AuctionItemFactory.create(ItemType.valueOf(rs.getString("item_type")), JdbcSupport.uuid(rs,"id"), JdbcSupport.dateTime(rs,"created_at"), JdbcSupport.dateTime(rs,"updated_at"), JdbcSupport.uuid(rs,"seller_id"), rs.getString("title"), rs.getString("description"), rs.getBigDecimal("starting_price"), rs.getString("attribute_one"), rs.getString("attribute_two"));
    }
}
