package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.PlaceBidRequest;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.server.model.Auction;
import com.auctionapp.auctionappjava.server.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionServiceConcurrencyTest {

    private AuctionService auctionService;
    private AuctionServiceTest.FakeAuctionDao fakeAuctionDao;
    private AuctionServiceTest.FakeBidDao fakeBidDao;
    private AuctionServiceTest.FakeUserDao fakeUserDao;

    @BeforeEach
    public void setUp() throws Exception {
        auctionService = new AuctionService();

        fakeAuctionDao = new AuctionServiceTest.FakeAuctionDao();
        fakeBidDao = new AuctionServiceTest.FakeBidDao();
        fakeUserDao = new AuctionServiceTest.FakeUserDao();

        setPrivateField("auctionDao", fakeAuctionDao);
        setPrivateField("itemDao", new AuctionServiceTest.FakeItemDao());
        setPrivateField("bidDao", fakeBidDao);
        setPrivateField("autoBidDao", new AuctionServiceTest.FakeAutoBidDao());
        setPrivateField("userDao", fakeUserDao);
    }

    @Test
    public void testPlaceBid_concurrentSameAuction_allowsOnlyOneBidAtSamePrice() throws Exception {
        // Test da luong:
        // Hai user cung luc cung dat 120 vao cung mot auction dang co gia 100.
        // Do AuctionService khoa theo auctionId, mot request vao truoc se thanh cong,
        // request con lai doc gia moi la 120 va bi tu choi vi khong cao hon gia hien tai.
        UUID auctionId = UUID.randomUUID();
        UUID bidder1Id = UUID.randomUUID();
        UUID bidder2Id = UUID.randomUUID();

        fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
        fakeUserDao.putWallet(wallet(bidder1Id, "1000"));
        fakeUserDao.putWallet(wallet(bidder2Id, "1000"));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        Thread t1 = new Thread(() -> placeBidAfterStart(ready, start, results, auctionId, bidder1Id));
        Thread t2 = new Thread(() -> placeBidAfterStart(ready, start, results, auctionId, bidder2Id));

        t1.start();
        t2.start();
        assertTrue(ready.await(1, TimeUnit.SECONDS));
        start.countDown();
        t1.join();
        t2.join();

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        Auction auction = fakeAuctionDao.findById(auctionId).orElseThrow();

        assertEquals(1, successCount);
        assertEquals(new BigDecimal("120"), auction.getCurrentPrice());
        assertEquals(1, fakeBidDao.findByAuctionId(auctionId).size());
    }

    private void placeBidAfterStart(CountDownLatch ready,
                                    CountDownLatch start,
                                    List<Boolean> results,
                                    UUID auctionId,
                                    UUID bidderId) {
        try {
            ready.countDown();
            start.await();
            var response = auctionService.handlePlaceBid(
                    new PlaceBidRequest(auctionId, bidderId, new BigDecimal("120"))
            );
            results.add(response.success());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            results.add(false);
        }
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = auctionService.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(auctionService, value);
    }

    private Auction runningAuction(UUID auctionId, BigDecimal currentPrice, UUID leadingBidderId) {
        return new Auction(
                auctionId,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(10),
                UUID.randomUUID(),
                UUID.randomUUID(),
                currentPrice,
                leadingBidderId,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusMinutes(20),
                AuctionStatus.RUNNING,
                new BigDecimal("10"),
                null
        );
    }

    private Wallet wallet(UUID userId, String balance) {
        return new Wallet(
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                userId,
                new BigDecimal(balance)
        );
    }
}
