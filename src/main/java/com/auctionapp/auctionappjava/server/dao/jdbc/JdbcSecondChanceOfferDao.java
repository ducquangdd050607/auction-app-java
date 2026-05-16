package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.enums.SecondChanceOfferStatus;
import com.auctionapp.auctionappjava.common.model.SecondChanceOffer;
import com.auctionapp.auctionappjava.server.dao.SecondChanceOfferDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcSecondChanceOfferDao extends JdbcDaoSupport implements SecondChanceOfferDao {
    public JdbcSecondChanceOfferDao() {
        ensureTable();
    }

    @Override
    public SecondChanceOffer save(SecondChanceOffer offer) {
        String sql = """
                INSERT INTO second_chance_offers (
                    id, auction_id, original_winner_id, offered_bidder_id, offer_amount,
                    expires_at, responded_at, status, note, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    auction_id = VALUES(auction_id), original_winner_id = VALUES(original_winner_id),
                    offered_bidder_id = VALUES(offered_bidder_id), offer_amount = VALUES(offer_amount),
                    expires_at = VALUES(expires_at), responded_at = VALUES(responded_at),
                    status = VALUES(status), note = VALUES(note), updated_at = VALUES(updated_at)
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, offer);
            statement.executeUpdate();
            return offer;
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong luu duoc second chance offer", exception);
        }
    }

    @Override
    public Optional<SecondChanceOffer> findById(UUID id) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM second_chance_offers WHERE id = ?")) {
            statement.setString(1, uuid(id));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc second chance offer", exception);
        }
    }

    @Override
    public List<SecondChanceOffer> findByAuctionId(UUID auctionId) {
        String sql = "SELECT * FROM second_chance_offers WHERE auction_id = ? ORDER BY created_at DESC";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(auctionId));
            return readMany(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc second chance offer theo auction", exception);
        }
    }

    @Override
    public List<SecondChanceOffer> findByStatus(SecondChanceOfferStatus status) {
        String sql = "SELECT * FROM second_chance_offers WHERE status = ? ORDER BY expires_at";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            return readMany(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc second chance offer theo status", exception);
        }
    }

    @Override
    public List<SecondChanceOffer> findExpiredOpenOffers(LocalDateTime now) {
        String sql = "SELECT * FROM second_chance_offers WHERE status = ? AND expires_at <= ? ORDER BY expires_at";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SecondChanceOfferStatus.OFFERED.name());
            statement.setTimestamp(2, timestamp(now));
            return readMany(statement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc second chance offer qua han", exception);
        }
    }

    private List<SecondChanceOffer> readMany(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            List<SecondChanceOffer> offers = new ArrayList<>();
            while (resultSet.next()) {
                offers.add(map(resultSet));
            }
            return offers;
        }
    }

    private void bind(PreparedStatement statement, SecondChanceOffer offer) throws SQLException {
        statement.setString(1, uuid(offer.getId()));
        statement.setString(2, uuid(offer.getAuctionId()));
        statement.setString(3, uuid(offer.getOriginalWinnerId()));
        statement.setString(4, uuid(offer.getOfferedBidderId()));
        statement.setBigDecimal(5, offer.getOfferAmount());
        statement.setTimestamp(6, timestamp(offer.getExpiresAt()));
        statement.setTimestamp(7, timestamp(offer.getRespondedAt()));
        statement.setString(8, offer.getStatus().name());
        statement.setString(9, offer.getNote());
        statement.setTimestamp(10, timestamp(offer.getCreatedAt()));
        statement.setTimestamp(11, timestamp(offer.getUpdatedAt()));
    }

    private SecondChanceOffer map(ResultSet resultSet) throws SQLException {
        return new SecondChanceOffer(
                uuid(resultSet.getString("id")),
                localDateTime(resultSet.getTimestamp("created_at")),
                localDateTime(resultSet.getTimestamp("updated_at")),
                uuid(resultSet.getString("auction_id")),
                uuid(resultSet.getString("original_winner_id")),
                uuid(resultSet.getString("offered_bidder_id")),
                resultSet.getBigDecimal("offer_amount"),
                localDateTime(resultSet.getTimestamp("expires_at")),
                localDateTime(resultSet.getTimestamp("responded_at")),
                SecondChanceOfferStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("note"));
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS second_chance_offers (
                    id VARCHAR(36) PRIMARY KEY,
                    auction_id VARCHAR(36) NOT NULL,
                    original_winner_id VARCHAR(36) NOT NULL,
                    offered_bidder_id VARCHAR(36) NOT NULL,
                    offer_amount DECIMAL(19, 2) NOT NULL,
                    expires_at DATETIME NOT NULL,
                    responded_at DATETIME NULL,
                    status VARCHAR(30) NOT NULL,
                    note VARCHAR(255),
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    UNIQUE KEY uq_second_chance_offer (auction_id, offered_bidder_id),
                    CONSTRAINT fk_second_chance_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                    CONSTRAINT fk_second_chance_original_winner FOREIGN KEY (original_winner_id) REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_second_chance_offered_bidder FOREIGN KEY (offered_bidder_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong tao duoc bang second_chance_offers", exception);
        }
    }
}
