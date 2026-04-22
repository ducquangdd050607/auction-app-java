package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.model.Item;
import com.auctionapp.auctionappjava.server.dao.AuctionItemDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.util.*;

public class JdbcAuctionItemDao implements AuctionItemDao {
    private final DatabaseManager db;

    public JdbcAuctionItemDao(DatabaseManager db) {
        this.db = db;
    }

    public Item save(Item i) {
        String sql = "INSERT INTO auction_items(id,created_at,updated_at,seller_id,title,description,item_type,starting_price,attribute_one,attribute_two) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, i);
            ps.executeUpdate();
            return i;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<Item> findById(UUID id) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM auction_items WHERE id=?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(item(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void update(Item i) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE auction_items SET updated_at=NOW(),title=?,description=?,starting_price=?,attribute_one=?,attribute_two=? WHERE id=?")) {
            ps.setString(1, i.getTitle());
            ps.setString(2, i.getDescription());
            ps.setBigDecimal(3, i.getStartingPrice());
            ps.setString(4, i.getAttributeOne());
            ps.setString(5, i.getAttributeTwo());
            ps.setString(6, i.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void delete(UUID id) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM auction_items WHERE id=?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fill(PreparedStatement ps, Item i) throws SQLException {
        ps.setString(1, i.getId().toString());
        ps.setTimestamp(2, JdbcSupport.ts(i.getCreatedAt()));
        ps.setTimestamp(3, JdbcSupport.ts(i.getUpdatedAt()));
        ps.setString(4, i.getSellerId().toString());
        ps.setString(5, i.getTitle());
        ps.setString(6, i.getDescription());
        ps.setString(7, i.getItemType().name());
        ps.setBigDecimal(8, i.getStartingPrice());
        ps.setString(9, i.getAttributeOne());
        ps.setString(10, i.getAttributeTwo());
    }

    private Item item(ResultSet rs) throws SQLException {
        return AuctionItemFactory.create(ItemType.valueOf(rs.getString("item_type")), UUID.fromString(rs.getString("id")), JdbcSupport.ldt(rs.getTimestamp("created_at")), JdbcSupport.ldt(rs.getTimestamp("updated_at")), UUID.fromString(rs.getString("seller_id")), rs.getString("title"), rs.getString("description"), rs.getBigDecimal("starting_price"), rs.getString("attribute_one"), rs.getString("attribute_two"));
    }
}
