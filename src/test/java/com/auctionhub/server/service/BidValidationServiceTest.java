package com.auctionhub.server.service;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.exception.ValidationException;
import com.auctionhub.common.model.Auction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BidValidationServiceTest {
    private final BidValidationService service = new BidValidationService();

    @Test
    void shouldAcceptValidBid() {
        Auction auction = sampleAuction();
        UUID bidderId = UUID.randomUUID();
        assertDoesNotThrow(() -> service.validateBid(auction, bidderId, BigDecimal.valueOf(130), LocalDateTime.now()));
    }

    @Test
    void shouldRejectLowBid() {
        Auction auction = sampleAuction();
        UUID bidderId = UUID.randomUUID();
        assertThrows(ValidationException.class,
                () -> service.validateBid(auction, bidderId, BigDecimal.valueOf(109), LocalDateTime.now()));
    }

    @Test
    void shouldRejectSellerSelfBid() {
        Auction auction = sampleAuction();
        assertThrows(ValidationException.class,
                () -> service.validateBid(auction, auction.getSellerId(), BigDecimal.valueOf(120), LocalDateTime.now()));
    }

    private Auction sampleAuction() {
        LocalDateTime now = LocalDateTime.now();
        return new Auction(
                UUID.randomUUID(), now, now,
                UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(100), null,
                now.minusMinutes(10), now.plusMinutes(10), AuctionStatus.RUNNING,
                BigDecimal.TEN, null);
    }
}
