package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;

import java.io.Serializable;
import java.math.BigDecimal;

public record AuctionTrendResponse(
        String auctionId,
        String itemName,
        String category,
        BigDecimal currentPrice,
        AuctionStatus status,
        int bidderCount,
        int bidCount,
        BigDecimal trendScore,
        String trendLabel,
        String reason
) implements Serializable {
}
