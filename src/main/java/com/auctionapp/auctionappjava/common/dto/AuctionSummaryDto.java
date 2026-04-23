package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionSummaryDto(
        UUID auctionId,
        UUID itemId,
        UUID sellerId,
        String title,
        String description,
        ItemType itemType,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        BigDecimal minimumIncrement,
        AuctionStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        UUID leadingBidderId,
        String leadingBidderName,
        UUID winnerId,
        long bidCount
) implements Serializable {}