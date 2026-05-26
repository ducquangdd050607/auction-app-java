package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;

import java.io.Serializable;
import java.math.BigDecimal;

public record AuctionSummaryResponse(
        String auctionId,
        String category,
        String itemName,
        String sellerName,
        String description,
        BigDecimal startPrice,     // Khởi đầu
        BigDecimal currentPrice,   // Giá hiện tại
        BigDecimal minimumIncrement,    // Bước giá
        String startDateTime,
        String endDateTime,
        long timeLeft, // Thời gian còn lại
        AuctionStatus status,
        int bidderCount,// Số bidder quan tâm
        byte[] imageData,
        int bots
) implements Serializable {}

// 1. Lần này là lần cuối thay đổi (mong vậy)
// 2. Usage trải dài lên mọi thứ -> Cho đầy đủ các tính chất cho AuctionList, Dashboard, RankingList(WIP)