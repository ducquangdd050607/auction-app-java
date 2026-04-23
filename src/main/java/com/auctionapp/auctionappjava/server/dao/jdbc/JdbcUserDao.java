package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcUserDao implements UserDao {
    private final DatabaseManager databaseManager;

    public JdbcUserDao() { this(new DatabaseManager()); }
    public JdbcUserDao(DatabaseManager databaseManager) { this.databaseManager = databaseManager; }

    @Override public void save(User user) {
        String sql = "INSERT INTO users (id, username, password_hash, password_salt, full_name, email, role, active, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getId().toString()); ps.setString(2, user.getUsername()); ps.setString(3, user.getPasswordHash()); ps.setString(4, user.getPasswordSalt());
            ps.setString(5, user.getFullName()); ps.setString(6, user.getEmail()); ps.setString(7, user.getRole().name()); ps.setBoolean(8, user.isActive());
            ps.setObject(9, user.getCreatedAt()); ps.setObject(10, user.getUpdatedAt()); ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Không thể lưu user", e); }
    }

    @Override public void update(User user) {
        user.touch();
        String sql = "UPDATE users SET username=?, password_hash=?, password_salt=?, full_name=?, email=?, role=?, active=?, updated_at=? WHERE id=?";
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername()); ps.setString(2, user.getPasswordHash()); ps.setString(3, user.getPasswordSalt()); ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail()); ps.setString(6, user.getRole().name()); ps.setBoolean(7, user.isActive()); ps.setObject(8, user.getUpdatedAt()); ps.setString(9, user.getId().toString()); ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Không thể cập nhật user", e); }
    }

    @Override public Optional<User> findById(UUID id) { return one("SELECT * FROM users WHERE id=?", id.toString()); }
    @Override public Optional<User> findByUsername(String username) { return one("SELECT * FROM users WHERE username=?", username); }

    @Override public List<User> findAll() {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM users ORDER BY created_at DESC"); ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>(); while (rs.next()) list.add(map(rs)); return list;
        } catch (SQLException e) { throw new IllegalStateException("Không thể tải user", e); }
    }

    @Override public long countAll() { return count("SELECT COUNT(*) FROM users"); }
    @Override public long countByRole(Role role) { return count("SELECT COUNT(*) FROM users WHERE role=?", role.name()); }

    @Override public void updateActive(UUID userId, boolean active) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE users SET active=?, updated_at=? WHERE id=?")) {
            ps.setBoolean(1, active); ps.setObject(2, LocalDateTime.now()); ps.setString(3, userId.toString()); ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Không thể cập nhật trạng thái user", e); }
    }

    private Optional<User> one(String sql, String param) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new IllegalStateException("Không thể tải user", e); }
    }

    private long count(String sql, String... params) {
        try (Connection c = databaseManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        } catch (SQLException e) { throw new IllegalStateException("Không thể đếm user", e); }
    }

    private User map(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        UUID id = JdbcSupport.uuid(rs, "id"); LocalDateTime c = JdbcSupport.dateTime(rs, "created_at"); LocalDateTime u = JdbcSupport.dateTime(rs, "updated_at");
        String username = rs.getString("username"), hash = rs.getString("password_hash"), salt = rs.getString("password_salt"), fullName = rs.getString("full_name"), email = rs.getString("email");
        boolean active = rs.getBoolean("active");
        return switch (role) {
            case ADMIN -> new Admin(id, c, u, username, hash, salt, fullName, email, active);
            case SELLER -> new Seller(id, c, u, username, hash, salt, fullName, email, active);
            case BIDDER -> new Bidder(id, c, u, username, hash, salt, fullName, email, active);
        };
    }
}
