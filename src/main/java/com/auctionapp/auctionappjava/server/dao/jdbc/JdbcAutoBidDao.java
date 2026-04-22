package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.dao.AutoBidDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.util.*;

public class JdbcAutoBidDao implements AutoBidDao {
    private final DatabaseManager db;

    public JdbcAutoBidDao(DatabaseManager db) {
        this.db = db;
    }

    public AutoBidConfig saveOrUpdate(AutoBidConfig c) {
        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO auto_bid_configs(id,created_at,updated_at,auction_id,bidder_id,max_bid,increment_amount,enabled) VALUES(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE updated_at=NOW(),max_bid=VALUES(max_bid),increment_amount=VALUES(increment_amount),enabled=VALUES(enabled)")) {
            if (c.getId() == null) c.setId(UUID.randomUUID());
            ps.setString(1, c.getId().toString());
            ps.setTimestamp(2, JdbcSupport.ts(c.getCreatedAt()));
            ps.setTimestamp(3, JdbcSupport.ts(c.getUpdatedAt()));
            ps.setString(4, c.getAuctionId().toString());
            ps.setString(5, c.getBidderId().toString());
            ps.setBigDecimal(6, c.getMaxBid());
            ps.setBigDecimal(7, c.getIncrementAmount());
            ps.setBoolean(8, c.isEnabled());
            ps.executeUpdate();
            return c;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<AutoBidConfig> findEnabledByAuction(UUID auctionId) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM auto_bid_configs WHERE auction_id=? AND enabled=TRUE ORDER BY created_at ASC")) {
            ps.setString(1, auctionId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<AutoBidConfig> out = new ArrayList<>();
                while (rs.next()) out.add(config(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private AutoBidConfig config(ResultSet rs) throws SQLException {
        return new AutoBidConfig(UUID.fromString(rs.getString("id")), JdbcSupport.ldt(rs.getTimestamp("created_at")), JdbcSupport.ldt(rs.getTimestamp("updated_at")), UUID.fromString(rs.getString("auction_id")), UUID.fromString(rs.getString("bidder_id")), rs.getBigDecimal("max_bid"), rs.getBigDecimal("increment_amount"), rs.getBoolean("enabled"));
    }
}
