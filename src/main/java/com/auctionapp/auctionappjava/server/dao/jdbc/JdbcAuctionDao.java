package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryDto;
import com.auctionapp.auctionappjava.common.enums.*;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.util.*;

public class JdbcAuctionDao implements AuctionDao {
    private final DatabaseManager db;

    public JdbcAuctionDao(DatabaseManager db) {
        this.db = db;
    }

    public Auction save(Auction a) {
        String sql = "INSERT INTO auctions(id,created_at,updated_at,item_id,seller_id,current_price,leading_bidder_id,start_time,end_time,status,minimum_increment,winner_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, a);
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<Auction> findById(UUID id) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM auctions WHERE id=?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(auction(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<AuctionSummaryDto> findSummaries() {
        return summaries("", null);
    }

    public List<AuctionSummaryDto> findSummariesBySeller(UUID sellerId) {
        return summaries("WHERE a.seller_id=?", sellerId.toString());
    }

    public void update(Auction a) {
        a.touch();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE auctions SET updated_at=?,item_id=?,seller_id=?,current_price=?,leading_bidder_id=?,start_time=?,end_time=?,status=?,minimum_increment=?,winner_id=? WHERE id=?")) {
            ps.setTimestamp(1, JdbcSupport.ts(a.getUpdatedAt()));
            ps.setString(2, a.getItemId().toString());
            ps.setString(3, a.getSellerId().toString());
            ps.setBigDecimal(4, a.getCurrentPrice());
            ps.setString(5, JdbcSupport.uuid(a.getLeadingBidderId()));
            ps.setTimestamp(6, JdbcSupport.ts(a.getStartTime()));
            ps.setTimestamp(7, JdbcSupport.ts(a.getEndTime()));
            ps.setString(8, a.getStatus().name());
            ps.setBigDecimal(9, a.getMinimumIncrement());
            ps.setString(10, JdbcSupport.uuid(a.getWinnerId()));
            ps.setString(11, a.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void updateStatus(UUID id, AuctionStatus s) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE auctions SET status=?,updated_at=NOW() WHERE id=?")) {
            ps.setString(1, s.name());
            ps.setString(2, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void delete(UUID id) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM auctions WHERE id=?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public long countAll() {
        return count("SELECT COUNT(*) FROM auctions", null);
    }

    public long countByStatus(AuctionStatus s) {
        return count("SELECT COUNT(*) FROM auctions WHERE status=?", s.name());
    }

    private long count(String sql, String p) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (p != null) ps.setString(1, p);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<AuctionSummaryDto> summaries(String where, String p) {
        String sql = "SELECT a.*,i.title,i.description,i.item_type,i.starting_price,u.username AS leader_name,(SELECT COUNT(*) FROM bids b WHERE b.auction_id=a.id) AS bid_count FROM auctions a JOIN auction_items i ON i.id=a.item_id LEFT JOIN users u ON u.id=a.leading_bidder_id " + where + " ORDER BY a.end_time ASC";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (p != null) ps.setString(1, p);
            try (ResultSet rs = ps.executeQuery()) {
                List<AuctionSummaryDto> out = new ArrayList<>();
                while (rs.next()) out.add(summary(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private AuctionSummaryDto summary(ResultSet rs) throws SQLException {
        return new AuctionSummaryDto(UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("item_id")), UUID.fromString(rs.getString("seller_id")), rs.getString("title"), rs.getString("description"), ItemType.valueOf(rs.getString("item_type")), rs.getBigDecimal("starting_price"), rs.getBigDecimal("current_price"), rs.getBigDecimal("minimum_increment"), AuctionStatus.valueOf(rs.getString("status")), JdbcSupport.ldt(rs.getTimestamp("start_time")), JdbcSupport.ldt(rs.getTimestamp("end_time")), JdbcSupport.uuid(rs.getString("leading_bidder_id")), rs.getString("leader_name"), JdbcSupport.uuid(rs.getString("winner_id")), rs.getLong("bid_count"));
    }

    private void fill(PreparedStatement ps, Auction a) throws SQLException {
        ps.setString(1, a.getId().toString());
        ps.setTimestamp(2, JdbcSupport.ts(a.getCreatedAt()));
        ps.setTimestamp(3, JdbcSupport.ts(a.getUpdatedAt()));
        ps.setString(4, a.getItemId().toString());
        ps.setString(5, a.getSellerId().toString());
        ps.setBigDecimal(6, a.getCurrentPrice());
        ps.setString(7, JdbcSupport.uuid(a.getLeadingBidderId()));
        ps.setTimestamp(8, JdbcSupport.ts(a.getStartTime()));
        ps.setTimestamp(9, JdbcSupport.ts(a.getEndTime()));
        ps.setString(10, a.getStatus().name());
        ps.setBigDecimal(11, a.getMinimumIncrement());
        ps.setString(12, JdbcSupport.uuid(a.getWinnerId()));
    }

    private Auction auction(ResultSet rs) throws SQLException {
        return new Auction(UUID.fromString(rs.getString("id")), JdbcSupport.ldt(rs.getTimestamp("created_at")), JdbcSupport.ldt(rs.getTimestamp("updated_at")), UUID.fromString(rs.getString("item_id")), UUID.fromString(rs.getString("seller_id")), rs.getBigDecimal("current_price"), JdbcSupport.uuid(rs.getString("leading_bidder_id")), JdbcSupport.ldt(rs.getTimestamp("start_time")), JdbcSupport.ldt(rs.getTimestamp("end_time")), AuctionStatus.valueOf(rs.getString("status")), rs.getBigDecimal("minimum_increment"), JdbcSupport.uuid(rs.getString("winner_id")));
    }
}
