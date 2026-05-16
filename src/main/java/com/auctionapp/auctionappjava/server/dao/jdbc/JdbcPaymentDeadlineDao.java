package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.PaymentDeadlineStatus;
import com.auctionapp.auctionappjava.common.model.PaymentDeadline;
import com.auctionapp.auctionappjava.server.dao.PaymentDeadlineDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcPaymentDeadlineDao extends JdbcDaoSupport implements PaymentDeadlineDao {
    public JdbcPaymentDeadlineDao() {
        ensureTable();
    }

    @Override
    public PaymentDeadline save(PaymentDeadline paymentDeadline) {
        String sql = """
                INSERT INTO payment_deadlines (
                    id, auction_id, winner_id, amount_due, deadline_at, paid_at, failed_at,
                    status, note, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    auction_id = VALUES(auction_id), winner_id = VALUES(winner_id),
                    amount_due = VALUES(amount_due), deadline_at = VALUES(deadline_at),
                    paid_at = VALUES(paid_at), failed_at = VALUES(failed_at),
                    status = VALUES(status), note = VALUES(note), updated_at = VALUES(updated_at)
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, paymentDeadline);
            statement.executeUpdate();
            return paymentDeadline;
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong luu duoc payment deadline", exception);
        }
    }

    @Override
    public Optional<PaymentDeadline> findById(UUID id) {
        return findOne("SELECT * FROM payment_deadlines WHERE id = ?", id);
    }

    @Override
    public Optional<PaymentDeadline> findByAuctionId(UUID auctionId) {
        return findOne("SELECT * FROM payment_deadlines WHERE auction_id = ? ORDER BY created_at DESC LIMIT 1", auctionId);
    }

    @Override
    public List<PaymentDeadline> findByStatus(PaymentDeadlineStatus status) {
        String sql = "SELECT * FROM payment_deadlines WHERE status = ? ORDER BY deadline_at";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            return readMany(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc payment deadline theo status", exception);
        }
    }

    @Override
    public List<PaymentDeadline> findExpiredPending(LocalDateTime now) {
        String sql = "SELECT * FROM payment_deadlines WHERE status = ? AND deadline_at <= ? ORDER BY deadline_at";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PaymentDeadlineStatus.PENDING.name());
            statement.setTimestamp(2, timestamp(now));
            return readMany(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc payment deadline qua han", exception);
        }
    }

    private Optional<PaymentDeadline> findOne(String sql, UUID id) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(id));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc payment deadline", exception);
        }
    }

    private List<PaymentDeadline> readMany(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            List<PaymentDeadline> deadlines = new ArrayList<>();
            while (resultSet.next()) {
                deadlines.add(map(resultSet));
            }
            return deadlines;
        }
    }

    private void bind(PreparedStatement statement, PaymentDeadline deadline) throws SQLException {
        statement.setString(1, uuid(deadline.getId()));
        statement.setString(2, uuid(deadline.getAuctionId()));
        statement.setString(3, uuid(deadline.getWinnerId()));
        statement.setBigDecimal(4, deadline.getAmountDue());
        statement.setTimestamp(5, timestamp(deadline.getDeadlineAt()));
        statement.setTimestamp(6, timestamp(deadline.getPaidAt()));
        statement.setTimestamp(7, timestamp(deadline.getFailedAt()));
        statement.setString(8, deadline.getStatus().name());
        statement.setString(9, deadline.getNote());
        statement.setTimestamp(10, timestamp(deadline.getCreatedAt()));
        statement.setTimestamp(11, timestamp(deadline.getUpdatedAt()));
    }

    private PaymentDeadline map(ResultSet resultSet) throws SQLException {
        return new PaymentDeadline(
                uuid(resultSet.getString("id")),
                localDateTime(resultSet.getTimestamp("created_at")),
                localDateTime(resultSet.getTimestamp("updated_at")),
                uuid(resultSet.getString("auction_id")),
                uuid(resultSet.getString("winner_id")),
                resultSet.getBigDecimal("amount_due"),
                localDateTime(resultSet.getTimestamp("deadline_at")),
                localDateTime(resultSet.getTimestamp("paid_at")),
                localDateTime(resultSet.getTimestamp("failed_at")),
                PaymentDeadlineStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("note"));
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS payment_deadlines (
                    id VARCHAR(36) PRIMARY KEY,
                    auction_id VARCHAR(36) NOT NULL,
                    winner_id VARCHAR(36) NOT NULL,
                    amount_due DECIMAL(19, 2) NOT NULL,
                    deadline_at DATETIME NOT NULL,
                    paid_at DATETIME NULL,
                    failed_at DATETIME NULL,
                    status VARCHAR(30) NOT NULL,
                    note VARCHAR(255),
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    UNIQUE KEY uq_payment_deadlines_auction (auction_id),
                    CONSTRAINT fk_payment_deadlines_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                    CONSTRAINT fk_payment_deadlines_winner FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong tao duoc bang payment_deadlines", exception);
        }
    }
}
