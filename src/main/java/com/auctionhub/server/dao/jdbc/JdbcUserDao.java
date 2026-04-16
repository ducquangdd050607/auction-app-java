package com.auctionhub.server.dao.jdbc;

import com.auctionhub.common.factory.UserFactory;
import com.auctionhub.common.model.User;
import com.auctionhub.server.dao.UserDao;
import com.auctionhub.server.db.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserDao implements UserDao {
    private final DatabaseManager databaseManager;

    public JdbcUserDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users (id, username, password_hash, password_salt, full_name, email, role, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId().toString());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPasswordSalt());
            statement.setString(5, user.getFullName());
            statement.setString(6, user.getEmail());
            statement.setString(7, user.getRole().name());
            statement.setBoolean(8, user.isActive());
            statement.setObject(9, user.getCreatedAt());
            statement.setObject(10, user.getUpdatedAt());
            statement.executeUpdate();
            return user;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể lưu user", ex);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        return queryOne("SELECT * FROM users WHERE id = ?", statement -> statement.setString(1, id.toString()));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return queryOne("SELECT * FROM users WHERE username = ?", statement -> statement.setString(1, username));
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
            return users;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đọc danh sách user", ex);
        }
    }

    @Override
    public long count() {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đếm user", ex);
        }
    }

    private Optional<User> queryOne(String sql, StatementConfigurer configurer) {
        try (var connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            configurer.configure(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể truy vấn user", ex);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        return UserFactory.create(
                Enum.valueOf(com.auctionhub.common.enums.Role.class, resultSet.getString("role")),
                UUID.fromString(resultSet.getString("id")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("password_salt"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getBoolean("active"));
    }

    @FunctionalInterface
    private interface StatementConfigurer {
        void configure(PreparedStatement statement) throws SQLException;
    }
}
