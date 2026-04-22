package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.util.*;

public class JdbcUserDao implements UserDao {
    private final DatabaseManager db;

    public JdbcUserDao(DatabaseManager db) {
        this.db = db;
    }

    public User save(User u) {
        String sql = "INSERT INTO users(id,created_at,updated_at,username,password_hash,password_salt,full_name,email,role,active) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            fill(ps, u);
            ps.executeUpdate();
            return u;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot save user", e);
        }
    }

    public Optional<User> findById(UUID id) {
        return one("SELECT * FROM users WHERE id=?", id.toString());
    }

    public Optional<User> findByUsername(String username) {
        return one("SELECT * FROM users WHERE username=?", username);
    }

    public List<User> findAll() {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM users ORDER BY created_at DESC"); ResultSet rs = ps.executeQuery()) {
            List<User> out = new ArrayList<>();
            while (rs.next()) out.add(user(rs));
            return out;
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot list users", e);
        }
    }

    public void updateRole(UUID id, Role role) {
        exec("UPDATE users SET role=?,updated_at=NOW() WHERE id=?", role.name(), id.toString());
    }

    public void updateProfile(UUID id, String fullName, String email) {
        exec("UPDATE users SET full_name=?,email=?,updated_at=NOW() WHERE id=?", fullName, email, id.toString());
    }

    public void updatePassword(UUID id, String hash, String salt) {
        exec("UPDATE users SET password_hash=?,password_salt=?,updated_at=NOW() WHERE id=?", hash, salt, id.toString());
    }

    public long countAll() {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users"); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<User> one(String sql, String p) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(user(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot find user", e);
        }
    }

    private void exec(String sql, String... p) {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) ps.setString(i + 1, p[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void fill(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getId().toString());
        ps.setTimestamp(2, JdbcSupport.ts(u.getCreatedAt()));
        ps.setTimestamp(3, JdbcSupport.ts(u.getUpdatedAt()));
        ps.setString(4, u.getUsername());
        ps.setString(5, u.getPasswordHash());
        ps.setString(6, u.getPasswordSalt());
        ps.setString(7, u.getFullName());
        ps.setString(8, u.getEmail());
        ps.setString(9, u.getRole().name());
        ps.setBoolean(10, u.isActive());
    }

    private User user(ResultSet rs) throws SQLException {
        User u = UserFactory.create(Role.valueOf(rs.getString("role")));
        u.setId(UUID.fromString(rs.getString("id")));
        u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        u.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPasswordSalt(rs.getString("password_salt"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        return u;
    }
}
