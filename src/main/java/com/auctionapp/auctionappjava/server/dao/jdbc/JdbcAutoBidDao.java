package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.dao.AutoBidDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcAutoBidDao implements AutoBidDao {
    private final DatabaseManager databaseManager;

    public JdbcAutoBidDao() {
        this(new DatabaseManager());
    }

    public JdbcAutoBidDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void save(AutoBidConfig cfg) {
        String sql = "INSERT INTO auto_bid_configs (id,auction_id,bidder_id,max_bid,increment_amount,enabled,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, cfg);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu auto bid", e);
        }
    }

    @Override
    public void update(AutoBidConfig cfg) {
        cfg.touch();
        String sql = "UPDATE auto_bid_configs SET max_bid=?, increment_amount=?, enabled=?, updated_at=? WHERE auction_id=? AND bidder_id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, cfg.getMaxBid());
            ps.setBigDecimal(2, cfg.getIncrementAmount());
            ps.setBoolean(3, cfg.isEnabled());
            ps.setObject(4, cfg.getUpdatedAt());
            ps.setString(5, cfg.getAuctionId().toString());
            ps.setString(6, cfg.getBidderId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật auto bid", e);
        }
    }

    @Override
    public void upsert(AutoBidConfig cfg) {
        if (findByAuctionAndBidder(cfg.getAuctionId(), cfg.getBidderId()).isPresent()) update(cfg);
        else save(cfg);
    }

    @Override
    public Optional<AutoBidConfig> findByAuctionAndBidder(UUID auctionId, UUID bidderId) {
        List<AutoBidConfig> list = many("SELECT * FROM auto_bid_configs WHERE auction_id=? AND bidder_id=?", auctionId.toString(), bidderId.toString());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<AutoBidConfig> findEnabledByAuctionId(UUID auctionId) {
        return many("SELECT * FROM auto_bid_configs WHERE auction_id=? AND enabled=TRUE ORDER BY created_at ASC", auctionId.toString());
    }

    @Override
    public List<AutoBidConfig> findByBidderId(UUID bidderId) {
        return many("SELECT * FROM auto_bid_configs WHERE bidder_id=? ORDER BY created_at DESC", bidderId.toString());
    }

    @Override
    public void setEnabled(UUID auctionId, UUID bidderId, boolean enabled) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE auto_bid_configs SET enabled=?, updated_at=? WHERE auction_id=? AND bidder_id=?")) {
            ps.setBoolean(1, enabled);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, auctionId.toString());
            ps.setString(4, bidderId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể bật/tắt auto bid", e);
        }
    }

    @Override
    public void delete(UUID configId) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM auto_bid_configs WHERE id=?")) {
            ps.setString(1, configId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa auto bid", e);
        }
    }

    private void bind(PreparedStatement ps, AutoBidConfig c) throws SQLException {
        ps.setString(1, c.getId().toString());
        ps.setString(2, c.getAuctionId().toString());
        ps.setString(3, c.getBidderId().toString());
        ps.setBigDecimal(4, c.getMaxBid());
        ps.setBigDecimal(5, c.getIncrementAmount());
        ps.setBoolean(6, c.isEnabled());
        ps.setObject(7, c.getCreatedAt());
        ps.setObject(8, c.getUpdatedAt());
    }

    private List<AutoBidConfig> many(String sql, Object... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                List<AutoBidConfig> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải auto bid", e);
        }
    }

    private AutoBidConfig map(ResultSet rs) throws SQLException {
        return new AutoBidConfig(JdbcSupport.uuid(rs, "id"), JdbcSupport.dateTime(rs, "created_at"), JdbcSupport.dateTime(rs, "updated_at"), JdbcSupport.uuid(rs, "auction_id"), JdbcSupport.uuid(rs, "bidder_id"), rs.getBigDecimal("max_bid"), rs.getBigDecimal("increment_amount"), rs.getBoolean("enabled"));
    }
}
