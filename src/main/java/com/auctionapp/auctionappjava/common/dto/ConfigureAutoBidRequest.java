package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record ConfigureAutoBidRequest(
        UUID auctionId,
        UUID bidderId,
        BigDecimal maxBid,
        BigDecimal incrementAmount,
        boolean enabled
) implements Serializable {}
