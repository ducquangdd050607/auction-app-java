package com.auctionhub.server.service;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.Auction;
import com.auctionhub.common.model.AutoBidConfig;
import com.auctionhub.common.strategy.AntiSnipingExtensionStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoBidEngineTest {
    @Test
    void shouldResolveCompetingAutoBidsAndPickHigherMaxBidder() {
        AutoBidEngine engine = new AutoBidEngine(new AntiSnipingExtensionStrategy(30, 60));
        LocalDateTime now = LocalDateTime.now();
        UUID sellerId = UUID.randomUUID();
        UUID bidderA = UUID.randomUUID();
        UUID bidderB = UUID.randomUUID();

        Auction auction = new Auction(UUID.randomUUID(), now, now, UUID.randomUUID(), sellerId,
                BigDecimal.valueOf(100), null, now.minusMinutes(5), now.plusMinutes(5), AuctionStatus.RUNNING,
                BigDecimal.TEN, null);

        AutoBidConfig a = new AutoBidConfig(UUID.randomUUID(), now.minusMinutes(2), now.minusMinutes(2), auction.getId(), bidderA,
                BigDecimal.valueOf(150), BigDecimal.TEN, true);
        AutoBidConfig b = new AutoBidConfig(UUID.randomUUID(), now.minusMinutes(1), now.minusMinutes(1), auction.getId(), bidderB,
                BigDecimal.valueOf(170), BigDecimal.TEN, true);

        AutoBidEngine.BidComputationResult result = engine.applyManualBid(auction, List.of(a, b), bidderA, BigDecimal.valueOf(110), now);

        assertEquals(bidderB, result.auction().getLeadingBidderId());
        assertEquals(BigDecimal.valueOf(160), result.auction().getCurrentPrice());
    }

    @Test
    void shouldHonorEarlierRegistrationWhenMaxBidTies() {
        AutoBidEngine engine = new AutoBidEngine(new AntiSnipingExtensionStrategy(30, 60));
        LocalDateTime now = LocalDateTime.now();
        UUID sellerId = UUID.randomUUID();
        UUID earlier = UUID.randomUUID();
        UUID later = UUID.randomUUID();

        Auction auction = new Auction(UUID.randomUUID(), now, now, UUID.randomUUID(), sellerId,
                BigDecimal.valueOf(100), null, now.minusMinutes(5), now.plusMinutes(5), AuctionStatus.RUNNING,
                BigDecimal.TEN, null);

        AutoBidConfig first = new AutoBidConfig(UUID.randomUUID(), now.minusMinutes(3), now.minusMinutes(3), auction.getId(), earlier,
                BigDecimal.valueOf(150), BigDecimal.TEN, true);
        AutoBidConfig second = new AutoBidConfig(UUID.randomUUID(), now.minusMinutes(1), now.minusMinutes(1), auction.getId(), later,
                BigDecimal.valueOf(150), BigDecimal.TEN, true);

        AutoBidEngine.BidComputationResult result = engine.applyManualBid(auction, List.of(first, second), later, BigDecimal.valueOf(110), now);

        assertEquals(earlier, result.auction().getLeadingBidderId());
        assertEquals(BigDecimal.valueOf(150), result.auction().getCurrentPrice());
    }
}
