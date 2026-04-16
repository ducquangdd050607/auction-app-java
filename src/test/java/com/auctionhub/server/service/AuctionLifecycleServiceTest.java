package com.auctionhub.server.service;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.Auction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionLifecycleServiceTest {
    private final AuctionLifecycleService lifecycleService = new AuctionLifecycleService();

    @Test
    void shouldMoveFromOpenToRunningToFinished() {
        LocalDateTime now = LocalDateTime.now();
        UUID winnerId = UUID.randomUUID();
        Auction auction = new Auction(UUID.randomUUID(), now, now, UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(100), winnerId,
                now.plusMinutes(5), now.plusMinutes(20), AuctionStatus.OPEN,
                BigDecimal.TEN, null);

        lifecycleService.refreshStatus(auction, now.minusMinutes(1));
        assertEquals(AuctionStatus.OPEN, auction.getStatus());

        lifecycleService.refreshStatus(auction, now.plusMinutes(6));
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());

        lifecycleService.refreshStatus(auction, now.plusMinutes(21));
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
        assertEquals(winnerId, auction.getWinnerId());
    }

    @Test
    void shouldKeepPaidAsTerminalStatus() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction(UUID.randomUUID(), now, now, UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(100), UUID.randomUUID(),
                now.minusMinutes(30), now.minusMinutes(10), AuctionStatus.PAID,
                BigDecimal.TEN, UUID.randomUUID());

        boolean changed = lifecycleService.refreshStatus(auction, now);
        assertTrue(!changed);
        assertEquals(AuctionStatus.PAID, auction.getStatus());
    }
}
