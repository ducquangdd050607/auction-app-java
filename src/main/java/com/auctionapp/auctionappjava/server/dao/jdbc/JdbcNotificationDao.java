package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.exception.DatabaseException;
import com.auctionapp.auctionappjava.server.dao.NotificationDao;
import com.auctionapp.auctionappjava.server.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcNotificationDao extends JdbcDaoSupport implements NotificationDao {
    @Override
    public void createNotification(UUID userId, UUID auctionId, String type, String message) {
        String sql = """
                INSERT INTO notifications (
                    id, user_id, auction_id, type, message, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(UUID.randomUUID()));
            statement.setString(2, uuid(userId));
            statement.setString(3, uuid(auctionId));
            statement.setString(4, type);
            statement.setString(5, message);
            statement.setTimestamp(6, timestamp(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Khong tao duoc thong bao", exception);
        }
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        String sql = """
                SELECT id, user_id, auction_id, type, message, created_at
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(userId));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Notification> notifications = new ArrayList<>();
                while (resultSet.next()) {
                    notifications.add(mapNotification(resultSet));
                }
                return notifications;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Khong doc duoc danh sach thong bao cua user", exception);
        }
    }


    @Override
    public void deleteByUserId(UUID userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ?";

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(userId));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Khong xoa duoc thong bao cua user", exception);
        }
    }


    private Notification mapNotification(ResultSet resultSet) throws SQLException {
        return new Notification(
                uuid(resultSet.getString("id")),
                uuid(resultSet.getString("user_id")),
                uuid(resultSet.getString("auction_id")),
                resultSet.getString("type"),
                resultSet.getString("message"),
                localDateTime(resultSet.getTimestamp("created_at"))
        );
    }
}
