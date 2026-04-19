package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.ItemType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionSummaryDto(UUID auctionId,
                                UUID itemId,
                                String title,
                                String description,
                                ItemType itemType,
                                BigDecimal startingPrice,
                                BigDecimal currentPrice,
                                BigDecimal minimumIncrement,
                                String sellerName,
                                String leadingBidderName,
                                LocalDateTime startTime,
                                LocalDateTime endTime,
                                AuctionStatus status,
                                long secondsRemaining,
                                boolean autoBidEnabledForViewer) implements Serializable {
}
