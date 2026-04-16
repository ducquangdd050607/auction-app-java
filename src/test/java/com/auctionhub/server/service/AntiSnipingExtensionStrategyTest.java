package com.auctionhub.server.service;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.Auction;
import com.auctionhub.common.strategy.AntiSnipingExtensionStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiSnipingExtensionStrategyTest {
    @Test
    void shouldExtendWhenBidArrivesInLastThresholdSeconds() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction(UUID.randomUUID(), now, now, UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(100), null, now.minusMinutes(10), now.plusSeconds(20), AuctionStatus.RUNNING,
                BigDecimal.TEN, null);
        AntiSnipingExtensionStrategy strategy = new AntiSnipingExtensionStrategy(30, 60);

        assertTrue(strategy.shouldExtend(auction, now));
        assertEquals(auction.getEndTime().plusSeconds(60), strategy.extendTo(auction, now));
    }

    @Test
    void shouldNotExtendWhenBidArrivesTooEarly() {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction(UUID.randomUUID(), now, now, UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(100), null, now.minusMinutes(10), now.plusMinutes(2), AuctionStatus.RUNNING,
                BigDecimal.TEN, null);
        AntiSnipingExtensionStrategy strategy = new AntiSnipingExtensionStrategy(30, 60);

        assertFalse(strategy.shouldExtend(auction, now));
    }
}
