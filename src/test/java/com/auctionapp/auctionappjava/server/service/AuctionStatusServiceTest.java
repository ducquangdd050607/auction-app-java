package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionStatusServiceTest {

    static class FakeAuctionDao implements AuctionDao {
        final Map<UUID, Auction> store = new HashMap<>();
        @Override public Auction save(Auction auction) { store.put(auction.getId(), auction); return auction; }
        @Override public Optional<Auction> findById(UUID auctionId) { return Optional.ofNullable(store.get(auctionId)); }
        @Override public List<Auction> findByStatus(com.auctionapp.auctionappjava.common.enums.AuctionStatus status) { return new ArrayList<>(); }
        @Override public List<Auction> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Auction> findBySellerId(UUID sellerId) { return new ArrayList<>(); }
        @Override public void deleteById(UUID auctionId) { store.remove(auctionId); }
        @Override
        public List<AuctionSummaryResponse> findAllSummaries() {
            return new ArrayList<>();
        }

        @Override
        public List<AuctionSummaryResponse> findSummariesBySellerId(UUID sellerId) {
            return new ArrayList<>();
        }
        @Override public Optional<Auction> findLatestAuctionCreatedBySellerId(UUID sellerId) { return Optional.empty(); }
        @Override public long countAuctionsCreatedBySellerId(UUID sellerId) { return 0; }
        @Override public Optional<Auction> findMostBiddedAuction() {return Optional.empty();}
    }

    private FakeAuctionDao fakeAuctionDao;

    @BeforeEach
    public void setUp() {
        fakeAuctionDao = new FakeAuctionDao();

        // Inject fake to private static field AuctionStatusService.auctionDao
        AuctionStatusService.setAuctionDao(fakeAuctionDao);
    }

    @Test
    public void testExecuteCloseAuction_setsFinished() throws InterruptedException {
        UUID auctionId = UUID.randomUUID();
        Auction a = new Auction(
                auctionId,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().minusMinutes(30),
                UUID.randomUUID(), UUID.randomUUID(),
                new java.math.BigDecimal("100"),
                null,
                LocalDateTime.now().minusMinutes(60),
                LocalDateTime.now().minusSeconds(1), // endTime already passed
                AuctionStatus.RUNNING,
                new java.math.BigDecimal("10"),
                null
        );
        fakeAuctionDao.save(a);

        // scheduleAuctionEvents should detect endTime <= now and call executeCloseAuction synchronously
        AuctionStatusService.scheduleAuctionEvents(a);

        // TODO: Do dính race condition phải xử lí bằng observer ở đây chưa làm nên tạm thời chặn luồng 1 lúc, nào làm realtime xong nhớ xóa
        Thread.sleep(100);

        Auction result = fakeAuctionDao.findById(auctionId).orElseThrow();
        assertEquals(AuctionStatus.FINISHED, result.getStatus(), "Auction should be marked FINISHED");
    }
}