package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcAuctionDao extends JdbcDaoSupport implements AuctionDao {
    @Override
    public Auction save(Auction auction) {
        String sql = """
                INSERT INTO auctions (
                    id, item_id, seller_id, current_price, leading_bidder_id, start_time, end_time,
                    status, minimum_increment, winner_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    item_id = VALUES(item_id), seller_id = VALUES(seller_id), current_price = VALUES(current_price),
                    leading_bidder_id = VALUES(leading_bidder_id), start_time = VALUES(start_time),
                    end_time = VALUES(end_time), status = VALUES(status), minimum_increment = VALUES(minimum_increment),
                    winner_id = VALUES(winner_id), updated_at = VALUES(updated_at)
                """;
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAuction(statement, auction);
            statement.executeUpdate();
            return auction;
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong luu duoc auction", exception);
        }
    }

    @Override
    public Optional<Auction> findById(UUID auctionId) {
        String sql = "SELECT * FROM auctions WHERE id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(auctionId));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAuction(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc auction", exception);
        }
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        String sql = "SELECT * FROM auctions WHERE status = ? ORDER BY created_at DESC";

        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            // Chuyển Enum thành String để lưu xuống DB
            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Auction> auctions = new ArrayList<>();
                while (resultSet.next()) {
                    auctions.add(mapAuction(resultSet));
                }
                return auctions;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc danh sach auction theo trang thai", exception);
        }
    }

    @Override
    public List<Auction> findAll() {
        return queryAuctions("SELECT * FROM auctions ORDER BY created_at", null);
    }

    @Override
    public List<Auction> findBySellerId(UUID sellerId) {
        return queryAuctions("SELECT * FROM auctions WHERE seller_id = ? ORDER BY created_at", sellerId);
    }

    @Override
    public List<AuctionSummaryResponse> findAllSummaries() {
        return queryAuctionSummaries("""
                SELECT
                    a.id AS auction_id,
                    i.item_type,
                    i.title,
                    u.full_name AS seller_name,
                    i.description,
                    i.starting_price,
                    a.current_price,
                    a.minimum_increment,
                    a.start_time,
                    a.end_time,
                    a.status,
                    COUNT(DISTINCT b.bidder_id) AS bidder_count
                FROM auctions a
                JOIN auction_items i ON i.id = a.item_id
                JOIN users u ON u.id = a.seller_id
                LEFT JOIN bids b ON b.auction_id = a.id
                GROUP BY
                    a.id, i.item_type, i.title, u.full_name, i.description,
                    i.starting_price, a.current_price, a.minimum_increment,
                    a.start_time, a.end_time, a.status, a.created_at
                ORDER BY a.created_at DESC
                """, null);
    }

    @Override
    public List<AuctionSummaryResponse> findSummariesBySellerId(UUID sellerId) {
        return queryAuctionSummaries("""
                SELECT
                    a.id AS auction_id,
                    i.item_type,
                    i.title,
                    u.full_name AS seller_name,
                    i.description,
                    i.starting_price,
                    a.current_price,
                    a.minimum_increment,
                    a.start_time,
                    a.end_time,
                    a.status,
                    COUNT(DISTINCT b.bidder_id) AS bidder_count
                FROM auctions a
                JOIN auction_items i ON i.id = a.item_id
                JOIN users u ON u.id = a.seller_id
                LEFT JOIN bids b ON b.auction_id = a.id
                WHERE a.seller_id = ?
                GROUP BY
                    a.id, i.item_type, i.title, u.full_name, i.description,
                    i.starting_price, a.current_price, a.minimum_increment,
                    a.start_time, a.end_time, a.status, a.created_at
                ORDER BY a.created_at DESC
                """, sellerId);
    }


    @Override
    public Optional<Auction> findLatestAuctionCreatedBySellerId(UUID sellerId) {
        String sql = "SELECT * FROM auctions WHERE seller_id = ? ORDER BY created_at ASC LIMIT 1";
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, uuid(sellerId));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAuction(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong tim duoc auction moi nhat cua seller: " + sellerId, exception);
        }
    }

    @Override
    public void deleteById(UUID auctionId) {
        String sql = "DELETE FROM auctions WHERE id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid(auctionId));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong xoa duoc auction", exception);
        }
    }

    @Override
    public long countAuctionsCreatedBySellerId(UUID sellerId) {
        String sql = "SELECT COUNT(DISTINCT item_id) FROM auctions WHERE seller_id = ?";

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set giá trị id của phiên đấu giá (nhớ parse UUID sang String vì DB bạn dùng VARCHAR(36))
            statement.setString(1, uuid(sellerId));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1); // Lấy kết quả đếm được trả về
                }
                return 0L; // Trả về 0 nếu chưa có ai đặt giá
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Khong dem duoc so luong auction cho seller: " + sellerId, exception);
        }
    }

    @Override
    public Optional<Auction> findMostBiddedAuction() {
        // JOIN với bảng bids, GROUP BY để đếm người tham gia (DISTINCT),
        // ORDER BY để đưa ông cao nhất lên đầu và LIMIT 1.
        String sql = """
            SELECT a.* FROM auctions a
            LEFT JOIN bids b ON a.id = b.auction_id
            WHERE a.status = 'RUNNING'
            GROUP BY a.id
            ORDER BY COUNT(DISTINCT b.bidder_id) DESC, a.created_at DESC
            LIMIT 1
            """;

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return Optional.of(mapAuction(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Lỗi khi tìm phiên đấu giá đông nhất", exception);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Auction> findMostExpiredAuction() {
        String sql = """
            SELECT * FROM auctions
            WHERE status = 'RUNNING'
            ORDER BY end_time ASC
            LIMIT 1
        """;
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return Optional.of(mapAuction(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Lỗi khi tìm phiên đấu giá sắp hết hạn", exception);
        }
        return Optional.empty();
    }

    private void bindAuction(PreparedStatement statement, Auction auction) throws SQLException {
        statement.setString(1, uuid(auction.getId()));
        statement.setString(2, uuid(auction.getItemId()));
        statement.setString(3, uuid(auction.getSellerId()));
        statement.setBigDecimal(4, auction.getCurrentPrice());
        statement.setString(5, uuid(auction.getLeadingBidderId()));
        statement.setTimestamp(6, timestamp(auction.getStartTime()));
        statement.setTimestamp(7, timestamp(auction.getEndTime()));
        statement.setString(8, auction.getStatus().name());
        statement.setBigDecimal(9, auction.getMinimumIncrement());
        statement.setString(10, uuid(auction.getWinnerId()));
        statement.setTimestamp(11, timestamp(auction.getCreatedAt()));
        statement.setTimestamp(12, timestamp(auction.getUpdatedAt()));
    }

    private List<Auction> queryAuctions(String sql, UUID sellerId) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (sellerId != null) {
                statement.setString(1, uuid(sellerId));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Auction> auctions = new ArrayList<>();
                while (resultSet.next()) {
                    auctions.add(mapAuction(resultSet));
                }
                return auctions;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc danh sach auction", exception);
        }
    }

    private List<AuctionSummaryResponse> queryAuctionSummaries(String sql, UUID sellerId) {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (sellerId != null) {
                statement.setString(1, uuid(sellerId));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AuctionSummaryResponse> summaries = new ArrayList<>();
                while (resultSet.next()) {
                    summaries.add(mapAuctionSummary(resultSet));
                }
                return summaries;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong doc duoc danh sach auction summary", exception);
        }
    }

    private AuctionSummaryResponse mapAuctionSummary(ResultSet resultSet) throws SQLException {
        return new AuctionSummaryResponse(
                resultSet.getString("auction_id"),
                resultSet.getString("item_type"),
                resultSet.getString("title"),
                resultSet.getString("seller_name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("starting_price"),
                resultSet.getBigDecimal("current_price"),
                resultSet.getBigDecimal("minimum_increment"),
                localDateTime(resultSet.getTimestamp("start_time")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                localDateTime(resultSet.getTimestamp("end_time")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                0,
                AuctionStatus.valueOf(resultSet.getString("status")),
                resultSet.getInt("bidder_count"),
                null
        );
    }


    private Auction mapAuction(ResultSet resultSet) throws SQLException {
        return new Auction(
                uuid(resultSet.getString("id")),
                localDateTime(resultSet.getTimestamp("created_at")),
                localDateTime(resultSet.getTimestamp("updated_at")),
                uuid(resultSet.getString("item_id")),
                uuid(resultSet.getString("seller_id")),
                resultSet.getBigDecimal("current_price"),
                uuid(resultSet.getString("leading_bidder_id")),
                localDateTime(resultSet.getTimestamp("start_time")),
                localDateTime(resultSet.getTimestamp("end_time")),
                AuctionStatus.valueOf(resultSet.getString("status")),
                resultSet.getBigDecimal("minimum_increment"),
                uuid(resultSet.getString("winner_id")));
    }
}
