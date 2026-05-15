package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionRealtimeEvent(
        String type,
        UUID bidId,
        UUID auctionId,
        UUID bidderId,
        String bidderName,
        BigDecimal currentPrice,
        BigDecimal bidAmount,
        UUID highestBidderId,
        LocalDateTime bidTime,
        LocalDateTime newEndTime,
        boolean autoBid,
        String message
) implements Serializable {
    public static final String BID_PLACED = "BID_PLACED";
    public static final String AUCTION_EXTENDED = "AUCTION_EXTENDED";
    public static final String AUCTION_FINISHED = "AUCTION_FINISHED";
    public static final String AUCTION_STATUS_CHANGED = "AUCTION_STATUS_CHANGED";
}
