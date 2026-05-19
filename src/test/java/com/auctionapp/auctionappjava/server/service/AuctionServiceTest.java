package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.BidHistoryResponse;
import com.auctionapp.auctionappjava.common.dto.BidRankingResponse;
import com.auctionapp.auctionappjava.common.dto.PlaceBidRequest;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.server.dao.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionServiceTest {

    private AuctionService auctionService;

    // Fake DAOs (in-memory) used by tests
    static class FakeAuctionDao implements AuctionDao {
        final Map<UUID, Auction> store = new HashMap<>();

        @Override public Auction save(Auction auction) { store.put(auction.getId(), auction); return auction; }
        @Override public Optional<Auction> findById(UUID auctionId) { return Optional.ofNullable(store.get(auctionId)); }
        @Override public List<Auction> findByStatus(com.auctionapp.auctionappjava.common.enums.AuctionStatus status) { return new ArrayList<>(); }
        @Override public List<Auction> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Auction> findBySellerId(UUID sellerId) { return new ArrayList<>(); }
        @Override public List<AuctionSummaryResponse> findAllSummaries() { return new ArrayList<>(); }
        @Override public List<AuctionSummaryResponse> findSummariesBySellerId(UUID sellerId) { return new ArrayList<>(); }
        @Override public void deleteById(UUID auctionId) { store.remove(auctionId); }

        // Additional helpers used by AuctionService elsewhere
        @Override public Optional<Auction> findLatestAuctionCreatedBySellerId(UUID sellerId) { return Optional.empty(); }
        @Override public long countAuctionsCreatedBySellerId(UUID sellerId) { return 0; }
        @Override public Optional<Auction> findMostExpiredAuction() {
            return store.values().stream()
                    .filter(auction -> auction.getStatus() == AuctionStatus.RUNNING)
                    .min(Comparator.comparing(Auction::getEndTime));
        }
        @Override public Optional<Auction> findMostBiddedAuction() {return Optional.empty();}
        @Override public long countRunningAuctions() {
            return store.values().stream()
                    .filter(auction -> auction.getStatus() == AuctionStatus.RUNNING)
                    .count();
        }
    }

    static class FakeItemDao implements AuctionItemDao {
        final Map<UUID, Item> store = new HashMap<>();

        @Override public Item save(Item item) { store.put(item.getId(), item); return item; }
        @Override public Optional<Item> findById(UUID itemId) { return Optional.ofNullable(store.get(itemId)); }
        @Override public Optional<byte[]> findImageByAuctionId(UUID auctionId) { return Optional.empty(); }
        @Override public Optional<Item> findByIdWithoutImage(UUID itemId) { return Optional.ofNullable(store.get(itemId)); }
        @Override public List<Item> findBySellerId(UUID sellerId) { return new ArrayList<>(); }
        @Override public void deleteById(UUID itemId) { store.remove(itemId); }

        // Implement required method from AuctionItemDao:
        @Override
        public Optional<Item> findByAuctionId(UUID auctionId) {
            // Test hiện tại không giữ mapping auctionId -> item, trả Optional.empty()
            // Nếu muốn test phụ thuộc, bạn có thể lưu mapping vào map bổ sung.
            return Optional.empty();
        }

        // Implement the oddly named method from interface (no-op for tests)
        @Override
        public void nookzzAll() {
            // no-op for tests
        }
    }

    static class FakeBidDao implements BidDao {
        final Map<UUID, BidTransaction> store = new LinkedHashMap<>(); // keep insertion order
        @Override public BidTransaction save(BidTransaction bidTransaction) { store.put(bidTransaction.getId(), bidTransaction); return bidTransaction; }
        @Override public List<BidTransaction> findByAuctionId(UUID auctionId) {
            List<BidTransaction> l = new ArrayList<>();
            for (BidTransaction b : store.values()) if (auctionId.equals(b.getAuctionId())) l.add(b);
            return l;
        }
        @Override public List<BidTransaction> findByBidderId(UUID bidderId) {
            List<BidTransaction> l = new ArrayList<>();
            for (BidTransaction b : store.values()) if (bidderId.equals(b.getBidderId())) l.add(b);
            return l;
        }
        @Override public List<BidTransaction> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<BidHistoryResponse> findHistoryByBidderId(UUID bidderId) {
            List<BidHistoryResponse> history = new ArrayList<>();
            for (BidTransaction bid : findByBidderId(bidderId)) {
                history.add(toHistoryResponse(null, bid));
            }
            return history;
        }
        @Override public List<BidHistoryResponse> findAllHistory() {
            List<BidHistoryResponse> history = new ArrayList<>();
            for (BidTransaction bid : store.values()) {
                history.add(toHistoryResponse("Test Bidder", bid));
            }
            return history;
        }
        @Override public List<BidRankingResponse> findRankingByAuctionId(UUID auctionId) {
            List<BidTransaction> bids = findByAuctionId(auctionId);
            bids.sort(Comparator
                    .comparing(BidTransaction::getAmount, Comparator.reverseOrder())
                    .thenComparing(BidTransaction::getCreatedAt));

            List<BidRankingResponse> ranking = new ArrayList<>();
            int rank = 1;
            for (BidTransaction bid : bids) {
                ranking.add(new BidRankingResponse(
                        rank++,
                        "Test Bidder",
                        bid.getAmount(),
                        bid.getCreatedAt() == null ? "Khong ro" : bid.getCreatedAt().toString(),
                        bid.isAutoGenerated()
                ));
            }
            return ranking;
        }
        @Override public long countByAuctionId(UUID auctionId) {
            return findByAuctionId(auctionId).size();
        }
        @Override public long countByBidderId(UUID bidderId) {
            return findByBidderId(bidderId).size();
        }
        @Override public long countBiddersByAuctionId(UUID auctionId) {
            return findByAuctionId(auctionId).stream().map(BidTransaction::getBidderId).distinct().count();
        }
        @Override public Optional<BidTransaction> findHighestBidByAuctionId(UUID auctionId) {
            return findByAuctionId(auctionId).stream().max(Comparator.comparing(BidTransaction::getAmount));
        }
        @Override public Optional<BidTransaction> findLatestBidByBidderId(UUID bidderId) {
            List<BidTransaction> bids = findByBidderId(bidderId);
            return bids.stream().max(Comparator.comparing(BidTransaction::getCreatedAt));
        }
        @Override public void deleteByAuctionId(UUID auctionId) {
            store.values().removeIf(b -> auctionId.equals(b.getAuctionId()));
        }

        // helper for tests
        public boolean hasSavedBidForAuction(UUID auctionId, BigDecimal amount) {
            return store.values().stream().anyMatch(b -> auctionId.equals(b.getAuctionId()) && amount.equals(b.getAmount()));
        }

        public boolean hasSavedAutoBidForAuction(UUID auctionId, UUID bidderId, BigDecimal amount) {
            return store.values().stream().anyMatch(b ->
                    auctionId.equals(b.getAuctionId())
                            && bidderId.equals(b.getBidderId())
                            && amount.equals(b.getAmount())
                            && b.isAutoGenerated());
        }

        @Override
        public long countBidsByBidderId(UUID bidderId) {
            // Đếm số auction khác nhau mà bidder này đã tham gia (cùng ý nghĩa như Jdbc impl)
            return findByBidderId(bidderId).stream()
                    .map(BidTransaction::getAuctionId)
                    .distinct()
                    .count();
        }
        private BidHistoryResponse toHistoryResponse(String bidderName, BidTransaction bid) {
            return new BidHistoryResponse(
                    bidderName,
                    "TEST",
                    "Test Auction",
                    BigDecimal.ZERO,
                    bid.getAmount(),
                    AuctionStatus.RUNNING,
                    bid.getUpdatedAt() == null ? "Khong ro" : bid.getUpdatedAt().toString()
            );
        }
    }

    static class FakeUserDao implements UserDao {
        final Map<UUID, User> users = new HashMap<>();
        final Map<UUID, Wallet> wallets = new HashMap<>();

        @Override public User save(User user) { users.put(user.getId(), user); return user; }
        @Override public Optional<Wallet> findWalletByUserId(UUID userId) { return Optional.ofNullable(wallets.get(userId)); }
        @Override public Optional<User> findById(UUID userId) { return Optional.ofNullable(users.get(userId)); }
        @Override public Optional<User> findByName(String username) { return users.values().stream().filter(u -> u.getUsername().equals(username)).findFirst(); }
        @Override public Wallet saveWallet(Wallet wallet) { wallets.put(wallet.getUserId(), wallet); return wallet; }
        @Override public void updateRole(UUID id, com.auctionapp.auctionappjava.common.enums.Role role) {}
        @Override public void updateProfile(UUID id, String fullName, String email) {}
        @Override public void updatePassword(UUID id, String hash, String salt) {}
        @Override public void updateActiveStatus(UUID id, boolean isActive) {}
        @Override public List<User> findAll() { return new ArrayList<>(users.values()); }
        @Override public long countUsersActive() {
            return users.values().stream()
                    .filter(User::isActive)
                    .count();
        }

        // helpers
        public void putUser(User u) { users.put(u.getId(), u); }
        public void putWallet(Wallet w) { wallets.put(w.getUserId(), w); }
    }

    static class FakeAutoBidDao implements AutoBidDao {
        final Map<String, AutoBidConfig> store = new HashMap<>();

        @Override
        public AutoBidConfig save(AutoBidConfig config) {
            store.put(key(config.getAuctionId(), config.getBidderId()), config);
            return config;
        }

        @Override
        public Optional<AutoBidConfig> findByAuctionIdAndBidderId(UUID auctionId, UUID bidderId) {
            return Optional.ofNullable(store.get(key(auctionId, bidderId)));
        }

        @Override
        public List<AutoBidConfig> findEnabledByAuctionId(UUID auctionId) {
            List<AutoBidConfig> configs = new ArrayList<>();
            for (AutoBidConfig config : store.values()) {
                if (auctionId.equals(config.getAuctionId()) && config.isEnabled()) {
                    configs.add(config);
                }
            }
            return configs;
        }

        @Override
        public void deleteByAuctionId(UUID auctionId) {
            store.values().removeIf(config -> auctionId.equals(config.getAuctionId()));
        }

        @Override
        public void disableByAuctionIdAndBidderId(UUID auctionId, UUID bidderId) {
            findByAuctionIdAndBidderId(auctionId, bidderId).ifPresent(config -> config.setEnabled(false));
        }

        private String key(UUID auctionId, UUID bidderId) {
            return auctionId + ":" + bidderId;
        }
    }

    private FakeAuctionDao fakeAuctionDao;
    private FakeItemDao fakeItemDao;
    private FakeBidDao fakeBidDao;
    private FakeAutoBidDao fakeAutoBidDao;
    private FakeUserDao fakeUserDao;

    @BeforeEach
    public void setUp() throws Exception {
        auctionService = new AuctionService();

        fakeAuctionDao = new FakeAuctionDao();
        fakeItemDao = new FakeItemDao();
        fakeBidDao = new FakeBidDao();
        fakeAutoBidDao = new FakeAutoBidDao();
        fakeUserDao = new FakeUserDao();

        // inject fakes into private final fields via reflection
        setPrivateField(auctionService, "auctionDao", fakeAuctionDao);
        setPrivateField(auctionService, "itemDao", fakeItemDao);
        setPrivateField(auctionService, "bidDao", fakeBidDao);
        setPrivateField(auctionService, "autoBidDao", fakeAutoBidDao);
        setPrivateField(auctionService, "userDao", fakeUserDao);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void testPlaceBid_successful() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();

        Auction auction = new Auction(
                auctionId, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().minusMinutes(5),
                UUID.randomUUID(), // itemId
                UUID.randomUUID(), // sellerId
                new BigDecimal("100"), // currentPrice
                null, // leadingBidderId
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(10),
                AuctionStatus.RUNNING,
                new BigDecimal("10"),
                null
        );
        fakeAuctionDao.save(auction);

        Wallet wallet = new Wallet(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), bidderId, new BigDecimal("500"));
        fakeUserDao.putWallet(wallet);
        // Create a user so userDao.findById works if needed
        User user = new Bidder();
        user.setId(bidderId);
        user.setUsername("tester");
        user.setFullName("Tester");
        fakeUserDao.putUser(user);

        PlaceBidRequest req = new PlaceBidRequest(auctionId, bidderId, new BigDecimal("120"));

        // Act
        var response = auctionService.handlePlaceBid(req);

        // Assert
        assertTrue(response.success(), "Expect place bid success");
        // Auction should be updated
        Auction updated = fakeAuctionDao.findById(auctionId).orElseThrow();
        assertEquals(new BigDecimal("120"), updated.getCurrentPrice());
        assertEquals(bidderId, updated.getLeadingBidderId());
        // Bid saved
        assertTrue(fakeBidDao.hasSavedBidForAuction(auctionId, new BigDecimal("120")));
        // Wallet deducted
        Wallet updatedWallet = fakeUserDao.findWalletByUserId(bidderId).orElseThrow();
        assertEquals(new BigDecimal("380"), updatedWallet.getBalance()); // 500 - 120 = 380
    }

    @Test
    public void testPlaceBid_tooLow() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();

        Auction auction = new Auction(
                auctionId, LocalDateTime.now(), LocalDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100"), null,
                LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(20),
                AuctionStatus.RUNNING, new BigDecimal("10"), null
        );
        fakeAuctionDao.save(auction);

        Wallet wallet = new Wallet(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), bidderId, new BigDecimal("1000"));
        fakeUserDao.putWallet(wallet);

        PlaceBidRequest req = new PlaceBidRequest(auctionId, bidderId, new BigDecimal("105")); // less than 110

        // Act
        var response = auctionService.handlePlaceBid(req);

        // Assert
        assertFalse(response.success());
        assertTrue(response.message().contains("Giá đặt phải từ"));
    }

    @Test
    public void testPlaceBid_insufficientBalance() {
        // Arrange
        UUID auctionId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();

        Auction auction = new Auction(
                auctionId, LocalDateTime.now(), LocalDateTime.now(),
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100"), null,
                LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(20),
                AuctionStatus.RUNNING, new BigDecimal("10"), null
        );
        fakeAuctionDao.save(auction);

        Wallet wallet = new Wallet(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), bidderId, new BigDecimal("50"));
        fakeUserDao.putWallet(wallet);

        PlaceBidRequest req = new PlaceBidRequest(auctionId, bidderId, new BigDecimal("200"));

        // Act
        var response = auctionService.handlePlaceBid(req);

        // Assert
        assertFalse(response.success());
        assertTrue(response.message().contains("Số dư trong ví không đủ"));
    }
}
