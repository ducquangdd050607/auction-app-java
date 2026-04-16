package com.auctionhub.server.db;

import com.auctionhub.common.dto.CreateAuctionRequest;
import com.auctionhub.common.enums.ItemType;
import com.auctionhub.common.enums.Role;
import com.auctionhub.common.factory.AuctionItemFactory;
import com.auctionhub.common.factory.UserFactory;
import com.auctionhub.common.model.Auction;
import com.auctionhub.common.model.AuctionItem;
import com.auctionhub.common.model.BidTransaction;
import com.auctionhub.common.model.User;
import com.auctionhub.common.util.PasswordUtils;
import com.auctionhub.server.config.ServerProperties;
import com.auctionhub.server.dao.AuctionDao;
import com.auctionhub.server.dao.AuctionItemDao;
import com.auctionhub.server.dao.BidDao;
import com.auctionhub.server.dao.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DatabaseInitializer {
    private final DatabaseManager databaseManager;
    private final ServerProperties properties;

    public DatabaseInitializer(DatabaseManager databaseManager, ServerProperties properties) {
        this.databaseManager = databaseManager;
        this.properties = properties;
    }

    public void initializeSchema() {
        String resource = properties.isMysql() ? "db/mysql-schema.sql" : "db/schema.sql";
        String sql = readResource(resource);
        try (Connection connection = databaseManager.getConnection(); var statement = connection.createStatement()) {
            for (String part : sql.split(";\s*\n")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể khởi tạo schema", ex);
        }
    }

    public void seedDemoData(UserDao userDao, AuctionItemDao auctionItemDao, AuctionDao auctionDao, BidDao bidDao) {
        if (userDao.count() > 0) {
            return;
        }

        User admin = createUser(Role.ADMIN, "admin", "admin123", "Quản trị viên", "admin@auctionhub.local");
        User sellerOne = createUser(Role.SELLER, "seller1", "seller123", "Nguyễn Seller", "seller1@auctionhub.local");
        User sellerTwo = createUser(Role.SELLER, "seller2", "seller123", "Trần Seller", "seller2@auctionhub.local");
        User bidderOne = createUser(Role.BIDDER, "bidder1", "bidder123", "Lê Bidder", "bidder1@auctionhub.local");
        User bidderTwo = createUser(Role.BIDDER, "bidder2", "bidder123", "Phạm Bidder", "bidder2@auctionhub.local");
        List<User> users = List.of(admin, sellerOne, sellerTwo, bidderOne, bidderTwo);
        users.forEach(userDao::save);

        LocalDateTime now = LocalDateTime.now();

        AuctionItem electronics = AuctionItemFactory.create(sellerOne.getId(),
                new CreateAuctionRequest(ItemType.ELECTRONICS, "MacBook Pro M3", "Laptop phục vụ demo realtime bidding", BigDecimal.valueOf(25_000_000), BigDecimal.valueOf(500_000), now.minusMinutes(10), now.plusMinutes(15), "Apple", "16GB/512GB"));
        auctionItemDao.save(electronics);
        Auction runningAuction = new Auction(UUID.randomUUID(), now.minusMinutes(10), now.minusMinutes(10), electronics.getId(), sellerOne.getId(), BigDecimal.valueOf(27_500_000), bidderOne.getId(), now.minusMinutes(5), now.plusMinutes(15), com.auctionhub.common.enums.AuctionStatus.RUNNING, BigDecimal.valueOf(500_000), null);
        auctionDao.save(runningAuction);

        AuctionItem art = AuctionItemFactory.create(sellerTwo.getId(),
                new CreateAuctionRequest(ItemType.ART, "Tranh sơn dầu Hạ Long", "Tác phẩm nghệ thuật demo chart giá", BigDecimal.valueOf(8_000_000), BigDecimal.valueOf(200_000), now.plusMinutes(5), now.plusMinutes(45), "Họa sĩ A", "Sơn dầu"));
        auctionItemDao.save(art);
        Auction openAuction = new Auction(UUID.randomUUID(), now.minusMinutes(1), now.minusMinutes(1), art.getId(), sellerTwo.getId(), BigDecimal.valueOf(8_000_000), null, now.plusMinutes(5), now.plusMinutes(45), com.auctionhub.common.enums.AuctionStatus.OPEN, BigDecimal.valueOf(200_000), null);
        auctionDao.save(openAuction);

        AuctionItem vehicle = AuctionItemFactory.create(sellerOne.getId(),
                new CreateAuctionRequest(ItemType.VEHICLE, "Honda SH 150i", "Xe demo trạng thái FINISHED", BigDecimal.valueOf(60_000_000), BigDecimal.valueOf(1_000_000), now.minusHours(2), now.minusHours(1), "Honda", "Biển số 59 demo"));
        auctionItemDao.save(vehicle);
        Auction finishedAuction = new Auction(UUID.randomUUID(), now.minusHours(2), now.minusHours(2), vehicle.getId(), sellerOne.getId(), BigDecimal.valueOf(67_000_000), bidderTwo.getId(), now.minusHours(2), now.minusHours(1), com.auctionhub.common.enums.AuctionStatus.FINISHED, BigDecimal.valueOf(1_000_000), bidderTwo.getId());
        auctionDao.save(finishedAuction);

        bidDao.save(new BidTransaction(UUID.randomUUID(), now.minusMinutes(4), now.minusMinutes(4), runningAuction.getId(), bidderOne.getId(), BigDecimal.valueOf(26_000_000), false, "Bid mở màn"));
        bidDao.save(new BidTransaction(UUID.randomUUID(), now.minusMinutes(2), now.minusMinutes(2), runningAuction.getId(), bidderOne.getId(), BigDecimal.valueOf(27_500_000), false, "Bid dẫn đầu hiện tại"));
        bidDao.save(new BidTransaction(UUID.randomUUID(), now.minusHours(1).minusMinutes(20), now.minusHours(1).minusMinutes(20), finishedAuction.getId(), bidderTwo.getId(), BigDecimal.valueOf(67_000_000), false, "Bid chiến thắng"));
    }

    private User createUser(Role role, String username, String rawPassword, String fullName, String email) {
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(rawPassword, salt);
        return UserFactory.create(role, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), username, hash, salt, fullName, email, true);
    }

    private String readResource(String resource) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalStateException("Không tìm thấy resource: " + resource);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể đọc file SQL: " + resource, ex);
        }
    }
}
