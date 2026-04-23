package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record AdminOverviewDto(
        long totalUsers,
        long totalAuctions,
        long runningAuctions,
        long finishedAuctions,
        BigDecimal totalBidVolume
) implements Serializable {
}