package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidHistoryPointDto(
        UUID bidId,
        UUID auctionId,
        UUID bidderId,
        String bidderName,
        BigDecimal amount,
        LocalDateTime bidTime,
        boolean autoBid
) implements Serializable {}
