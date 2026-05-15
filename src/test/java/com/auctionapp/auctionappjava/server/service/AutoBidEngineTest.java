package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoBidEngineTest {

    private final AutoBidEngine engine = new AutoBidEngine();

    @Test
    void resolveProxyBid_threeBidders_usesRunnerUpMaxBidPlusWinnerStep() {
        UUID auctionId = UUID.randomUUID();
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID userC = UUID.randomUUID();

        List<AutoBidConfig> configs = List.of(
                autoBid(auctionId, userA, "1000", "50"),
                autoBid(auctionId, userB, "700", "20"),
                autoBid(auctionId, userC, "500", "10")
        );

        var result = engine.resolveProxyBid(
                configs,
                new BigDecimal("400"),
                new BigDecimal("10"),
                null
        );

        assertTrue(result.isPresent());
        assertEquals(userA, result.get().winnerConfig().getBidderId());
        assertEquals(new BigDecimal("750"), result.get().finalPrice());
    }

    @Test
    void resolveProxyBid_capsBidAtWinnerMaxBid() {
        UUID auctionId = UUID.randomUUID();
        UUID topUser = UUID.randomUUID();

        List<AutoBidConfig> configs = List.of(
                autoBid(auctionId, topUser, "1000", "100"),
                autoBid(auctionId, UUID.randomUUID(), "980", "20")
        );

        var result = engine.resolveProxyBid(
                configs,
                new BigDecimal("400"),
                new BigDecimal("10"),
                null
        );

        assertTrue(result.isPresent());
        assertEquals(topUser, result.get().winnerConfig().getBidderId());
        assertEquals(new BigDecimal("1000"), result.get().finalPrice());
    }

    @Test
    void resolveProxyBid_singleBidder_usesCurrentPricePlusWinnerStep() {
        UUID auctionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var result = engine.resolveProxyBid(
                List.of(autoBid(auctionId, userId, "1000", "50")),
                new BigDecimal("400"),
                new BigDecimal("10"),
                null
        );

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().winnerConfig().getBidderId());
        assertEquals(new BigDecimal("450"), result.get().finalPrice());
    }

    private AutoBidConfig autoBid(UUID auctionId, UUID bidderId, String maxBid, String bidStep) {
        LocalDateTime now = LocalDateTime.now();
        return new AutoBidConfig(
                UUID.randomUUID(),
                now,
                now,
                auctionId,
                bidderId,
                new BigDecimal(maxBid),
                new BigDecimal(bidStep),
                true
        );
    }
}
