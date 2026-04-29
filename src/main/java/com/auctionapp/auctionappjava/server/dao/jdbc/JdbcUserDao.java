package com.auctionapp.auctionappjava.server.dao.jdbc;


import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.server.dao.UserDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserDao extends JdbcDaoSupport implements UserDao {
    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users (
                    id, username, password_hash, password_salt, full_name, email,
                    role, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    username = VALUES(username), password_hash = VALUES(password_hash),
                    password_salt = VALUES(password_salt), full_name = VALUES(full_name),
                    email = VALUES(email), role = VALUES(role), active = VALUES(active),
                    updated_at = VALUES(updated_at)
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bindUser(statement, user);
            statement.executeUpdate();
            return user;
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong luu duoc user", exception);
        }
    }
    @Override
    public Optional<Wallet> findWalletByUserId(UUID userId) {
        String sql = "SELECT * FROM Wallet WHERE user_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(userId));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapWallet(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc wallet", exception);
        }
    }
    @Override
    public Optional<User> findById(UUID userId) {
        return findUser("SELECT * FROM users WHERE id = ?", uuid(userId));
    }

    @Override
    public Optional<User> findByName(String username) {
        return findUser("SELECT * FROM users WHERE username = ?", username);
    }


    @Override
    public Wallet saveWallet(Wallet wallet) {
        String sql = """
                INSERT INTO Wallet (id, user_id, balance, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE balance = VALUES(balance), updated_at = VALUES(updated_at)
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(wallet.getId()));
            statement.setString(2, uuid(wallet.getUserId()));
            statement.setBigDecimal(3, wallet.getBalance());
            statement.setTimestamp(4, timestamp(wallet.getCreatedAt()));
            statement.setTimestamp(5, timestamp(wallet.getUpdatedAt()));
            statement.executeUpdate();
            return wallet;
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong luu duoc wallet", exception);
        }
    }

    public void updateRole(UUID id, Role role) {
        update("UPDATE users SET role=?,updated_at=NOW() WHERE id=?", role.name(), uuid(id));
    }

    public void updateProfile(UUID id, String fullName, String email) {
        update("UPDATE users SET full_name=?,email=?,updated_at=NOW() WHERE id=?", fullName, email, uuid(id));
    }

    public void updatePassword(UUID id, String hash, String salt) {
        update("UPDATE users SET password_hash=?,password_salt=?,updated_at=NOW() WHERE id=?", hash, salt, uuid(id));
    }

    private Optional<User> findUser(String sql, String value) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc user", exception);
        }
    }

    private void bindUser(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, uuid(user.getId()));
        statement.setString(2, user.getUsername());
        statement.setString(3, user.getPasswordHash());
        statement.setString(4, user.getPasswordSalt());
        statement.setString(5, user.getFullName());
        statement.setString(6, user.getEmail());
        statement.setString(7, user.getRole().name());
        statement.setBoolean(8, user.isActive());
        statement.setTimestamp(9, timestamp(user.getCreatedAt()));
        statement.setTimestamp(10, timestamp(user.getUpdatedAt()));
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = UserFactory.create(Role.valueOf(resultSet.getString("role")));
        user.setId(uuid(resultSet.getString("id")));
        user.setCreatedAt(localDateTime(resultSet.getTimestamp("created_at")));
        user.setUpdatedAt(localDateTime(resultSet.getTimestamp("updated_at")));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPasswordSalt(resultSet.getString("password_salt"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setActive(resultSet.getBoolean("active"));
        return user;
    }

    private void update(String sql, String... p) { // Java sẽ gom các biến trong ...thành 1 mảng []p
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < p.length; i++) {
                ps.setString(i + 1, p[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
    private Wallet mapWallet(ResultSet resultSet) throws SQLException {
        return new Wallet(
                uuid(resultSet.getString("id")),
                localDateTime(resultSet.getTimestamp("created_at")),
                localDateTime(resultSet.getTimestamp("updated_at")),
                uuid(resultSet.getString("user_id")),
                resultSet.getBigDecimal("balance"));
    }

}