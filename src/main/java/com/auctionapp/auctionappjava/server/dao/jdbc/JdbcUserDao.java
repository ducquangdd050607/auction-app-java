package com.auctionapp.auctionappjava.server.dao.jdbc;


import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserDao implements UserDao {
    private final DatabaseManager db;

    public JdbcUserDao(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO USER VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPasswordSalt());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getFullName());
            ps.setString(7, user.getRole().toString());
            ps.setTimestamp(8, JdbcSupport.ts(user.getCreatedAt()));
            ps.setTimestamp(9, JdbcSupport.ts(user.getUpdatedAt()));
            ps.setBoolean(10, user.isActive());

            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu người dùng", e);
        }

    }

    @Override
    public Optional<User> findById(UUID id) {
        one("SELECT * FROM USERS WHERE ID = ?", id.toString());
    }



    private Optional<User> one(String sql, String p) {
        try(Connection connection = db.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p);
            try( ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(user(rs)): Optional.empty();
            }

        }catch (SQLException e){
            throw new IllegalStateException("Không tìm thấy người dùng", e);
        }
    }

    private Optional<User> user(String rs){

    }
}