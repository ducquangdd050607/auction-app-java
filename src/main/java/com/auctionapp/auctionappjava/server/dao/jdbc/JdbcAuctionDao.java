package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcAuctionDao implements AuctionDao {
    private final DatabaseManager databaseManager;
    public JdbcAuctionDao() { this(new DatabaseManager()); }
    public JdbcAuctionDao(DatabaseManager databaseManager) { this.databaseManager = databaseManager; }

    @Override public void save(Auction a) {
        String sql = "INSERT INTO auctions (id,item_id,seller_id,current_price,leading_bidder_id,start_time,end_time,status,minimum_increment,winner_id,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { bindFull(ps,a); ps.executeUpdate(); }
        catch (SQLException e) { throw new IllegalStateException("Không thể lưu auction", e); }
    }

    @Override public void update(Auction a) {
        a.touch();
        String sql = "UPDATE auctions SET item_id=?, seller_id=?, current_price=?, leading_bidder_id=?, start_time=?, end_time=?, status=?, minimum_increment=?, winner_id=?, updated_at=? WHERE id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,a.getItemId().toString()); ps.setString(2,a.getSellerId().toString()); ps.setBigDecimal(3,a.getCurrentPrice()); ps.setString(4,JdbcSupport.str(a.getLeadingBidderId()));
            ps.setObject(5,a.getStartTime()); ps.setObject(6,a.getEndTime()); ps.setString(7,a.getStatus().name()); ps.setBigDecimal(8,a.getMinimumIncrement()); ps.setString(9,JdbcSupport.str(a.getWinnerId())); ps.setObject(10,a.getUpdatedAt()); ps.setString(11,a.getId().toString()); ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Không thể cập nhật auction", e); }
    }

    @Override public void delete(UUID auctionId) { exec("DELETE FROM auctions WHERE id=?", auctionId.toString()); }
    @Override public Optional<Auction> findById(UUID auctionId) { List<Auction> list = many("SELECT * FROM auctions WHERE id=?", auctionId.toString()); return list.isEmpty()?Optional.empty():Optional.of(list.get(0)); }
    @Override public List<Auction> findAll() { return many("SELECT * FROM auctions ORDER BY end_time ASC"); }
    @Override public List<Auction> findBySellerId(UUID sellerId) { return many("SELECT * FROM auctions WHERE seller_id=? ORDER BY created_at DESC", sellerId.toString()); }
    @Override public List<Auction> findByStatus(AuctionStatus status) { return many("SELECT * FROM auctions WHERE status=? ORDER BY end_time ASC", status.name()); }
    @Override public List<Auction> findEndingBefore(LocalDateTime now) { return many("SELECT * FROM auctions WHERE status=? AND end_time<=?", AuctionStatus.RUNNING.name(), now); }
    @Override public List<Auction> findStartingBefore(LocalDateTime now) { return many("SELECT * FROM auctions WHERE status=? AND start_time<=?", AuctionStatus.OPEN.name(), now); }
    @Override public long countAll() { return count("SELECT COUNT(*) FROM auctions"); }
    @Override public long countByStatus(AuctionStatus status) { return count("SELECT COUNT(*) FROM auctions WHERE status=?", status.name()); }

    private void exec(String sql, Object... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { for (int i=0;i<params.length;i++) ps.setObject(i+1, params[i]); ps.executeUpdate(); }
        catch (SQLException e) { throw new IllegalStateException("Không thể thao tác auction", e); }
    }
    private long count(String sql, Object... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { for (int i=0;i<params.length;i++) ps.setObject(i+1, params[i]); try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getLong(1):0L;} }
        catch (SQLException e) { throw new IllegalStateException("Không thể đếm auction", e); }
    }
    private List<Auction> many(String sql, Object... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { for (int i=0;i<params.length;i++) ps.setObject(i+1, params[i]); try(ResultSet rs=ps.executeQuery()){List<Auction> list=new ArrayList<>(); while(rs.next()) list.add(map(rs)); return list;} }
        catch (SQLException e) { throw new IllegalStateException("Không thể tải auction", e); }
    }
    private void bindFull(PreparedStatement ps, Auction a) throws SQLException {
        ps.setString(1,a.getId().toString()); ps.setString(2,a.getItemId().toString()); ps.setString(3,a.getSellerId().toString()); ps.setBigDecimal(4,a.getCurrentPrice()); ps.setString(5,JdbcSupport.str(a.getLeadingBidderId()));
        ps.setObject(6,a.getStartTime()); ps.setObject(7,a.getEndTime()); ps.setString(8,a.getStatus().name()); ps.setBigDecimal(9,a.getMinimumIncrement()); ps.setString(10,JdbcSupport.str(a.getWinnerId())); ps.setObject(11,a.getCreatedAt()); ps.setObject(12,a.getUpdatedAt());
    }
    private Auction map(ResultSet rs) throws SQLException {
        return new Auction(JdbcSupport.uuid(rs,"id"), JdbcSupport.dateTime(rs,"created_at"), JdbcSupport.dateTime(rs,"updated_at"), JdbcSupport.uuid(rs,"item_id"), JdbcSupport.uuid(rs,"seller_id"), rs.getBigDecimal("current_price"), JdbcSupport.uuid(rs,"leading_bidder_id"), JdbcSupport.dateTime(rs,"start_time"), JdbcSupport.dateTime(rs,"end_time"), AuctionStatus.valueOf(rs.getString("status")), rs.getBigDecimal("minimum_increment"), JdbcSupport.uuid(rs,"winner_id"));
    }
}
