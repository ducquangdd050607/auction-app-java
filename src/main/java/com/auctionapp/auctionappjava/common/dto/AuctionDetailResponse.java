package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record AuctionDetailResponse(
        String auctionId,
        String itemName,
        String description,
        String sellerName,
        BigDecimal startingPrice,
        BigDecimal currentPrice,
        BigDecimal stepPrice,
        long endTimeMillis,
        byte[] imageData,
        List<BidHistoryResponse> recentBids // Trả về 5-10 lượt bid gần nhất
) implements Serializable {}