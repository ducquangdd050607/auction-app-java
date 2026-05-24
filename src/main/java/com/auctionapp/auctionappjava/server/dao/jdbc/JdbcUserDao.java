package com.auctionapp.auctionappjava.server.dao.jdbc;


import com.auctionapp.auctionappjava.common.dto.UserDetailResponse;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.exception.DatabaseException;
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
import java.util.List;
import java.util.ArrayList;


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
            throw new DatabaseException("Khong luu duoc user", exception);
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
            throw new DatabaseException("Khong doc duoc wallet", exception);
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
            throw new DatabaseException("Khong luu duoc wallet", exception);
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

    @Override
    public void updateActiveStatus(UUID id, boolean isActive) {
        // Cập nhật cột active và tự động làm mới thời gian updated_at
        String sql = "UPDATE users SET active = ?, updated_at = NOW() WHERE id = ?";

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, isActive); // Bật (true) hoặc Tắt (false)
            statement.setString(2, uuid(id));  // Gọi hàm uuid() từ JdbcDaoSupport để parse UUID sang String

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new DatabaseException("Khong cap nhat duoc trang thai active cua user", exception);
        }
    }

    @Override
    public long countUsersActive() {
        String sql = "SELECT COUNT(DISTINCT id) FROM users WHERE active = 1";
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set giá trị id của phiên đấu giá (nhớ parse UUID sang String vì DB bạn dùng VARCHAR(36))

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1); // Lấy kết quả đếm được trả về
                }
                return 0L; // Trả về 0 nếu chưa có ai đặt giá
            }

        } catch (SQLException exception) {
            throw new DatabaseException("Khong dem duoc so luong users active", exception);
        }
    }

    private Optional<User> findUser(String sql, String value) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Khong doc duoc user", exception);
        }
    }

    @Override
    public Optional<User> findSellerByAuctionId(UUID auctionId) {
        String sql = """
        SELECT u.* FROM users u
        INNER JOIN auctions a ON u.id = a.seller_id
        WHERE a.id = ?
        """;

        return findUser(sql, uuid(auctionId));
    }

    @Override
    public List<UserDetailResponse> findAllDetails() {
        String sql = """
                SELECT
                    u.id,
                    u.full_name,
                    u.role,
                    COALESCE(w.balance, 0) AS balance,
                    u.active,
                    CASE
                        WHEN u.role = 'BIDDER' THEN COALESCE(bidder_stats.latest_item_title, 'Acc moi chua cuoc')
                        WHEN u.role = 'SELLER' THEN COALESCE(seller_stats.latest_item_title, 'Acc moi chua tao')
                        ELSE 'Admin'
                    END AS latest_item_title,
                    CASE
                        WHEN u.role = 'BIDDER' THEN COALESCE(bidder_stats.bid_count, 0)
                        WHEN u.role = 'SELLER' THEN COALESCE(seller_stats.auction_count, 0)
                        ELSE 0
                    END AS counters
                FROM users u
                LEFT JOIN Wallet w ON w.user_id = u.id
                LEFT JOIN (
                    SELECT
                        latest_bid.bidder_id,
                        i.title AS latest_item_title,
                        bid_counts.bid_count
                    FROM (
                        SELECT bidder_id, auction_id
                        FROM (
                            SELECT
                                b.bidder_id,
                                b.auction_id,
                                ROW_NUMBER() OVER (PARTITION BY b.bidder_id ORDER BY b.created_at DESC) AS rn
                            FROM bids b
                        ) ranked_bids
                        WHERE rn = 1
                    ) latest_bid
                    LEFT JOIN auctions a ON a.id = latest_bid.auction_id
                    LEFT JOIN auction_items i ON i.id = a.item_id
                    LEFT JOIN (
                        SELECT bidder_id, COUNT(DISTINCT auction_id) AS bid_count
                        FROM bids
                        GROUP BY bidder_id
                    ) bid_counts ON bid_counts.bidder_id = latest_bid.bidder_id
                ) bidder_stats ON bidder_stats.bidder_id = u.id
                LEFT JOIN (
                    SELECT
                        latest_auction.seller_id,
                        i.title AS latest_item_title,
                        seller_counts.auction_count
                    FROM (
                        SELECT seller_id, item_id
                        FROM (
                            SELECT
                                a.seller_id,
                                a.item_id,
                                ROW_NUMBER() OVER (PARTITION BY a.seller_id ORDER BY a.created_at ASC) AS rn
                            FROM auctions a
                        ) ranked_auctions
                        WHERE rn = 1
                    ) latest_auction
                    LEFT JOIN auction_items i ON i.id = latest_auction.item_id
                    LEFT JOIN (
                        SELECT seller_id, COUNT(DISTINCT item_id) AS auction_count
                        FROM auctions
                        GROUP BY seller_id
                    ) seller_counts ON seller_counts.seller_id = latest_auction.seller_id
                ) seller_stats ON seller_stats.seller_id = u.id
                ORDER BY u.created_at
                """;
//        Nó lấy các thông tin:
//        - Thông tin user: id, họ tên, role, trạng thái active.
//        - Số dư ví của user.
//        - Tên item gần nhất liên quan đến user.
//        - Số lần bidder tham gia đấu giá hoặc seller tạo phiên đấu giá.

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<UserDetailResponse> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(new UserDetailResponse(
                        resultSet.getString("id"),
                        resultSet.getString("latest_item_title"),
                        resultSet.getString("full_name"),
                        resultSet.getString("role"),
                        resultSet.getBigDecimal("balance"),
                        resultSet.getBoolean("active"),
                        resultSet.getInt("counters")
                ));
            }
            return users;
        } catch (SQLException exception) {
            throw new DatabaseException("Khong doc duoc danh sach user detail", exception);
        }
    }

    private List<User> queryUsers(String sql) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;

        } catch (SQLException exception) {
            throw new DatabaseException("Khong doc duoc danh sach user", exception);
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
            throw new DatabaseException("Khong cap nhat duoc user", e);
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
