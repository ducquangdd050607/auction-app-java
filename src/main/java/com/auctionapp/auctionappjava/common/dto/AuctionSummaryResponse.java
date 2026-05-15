package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionSummaryResponse(
        String auctionId,
        String category,
        String itemName,
        String sellerName,
        String description,
        BigDecimal startPrice,     // Khởi đầu
        BigDecimal currentPrice,   // Giá hiện tại
        BigDecimal minimumIncrement,    // Bước giá
<<<<<<< HEAD
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
=======
        String startDateTime,
        String endDateTime,
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
        long timeLeft, // Thời gian còn lại
        AuctionStatus status,
        int bidderCount,// Số bidder quan tâm
        byte[] imageData
) implements Serializable {}

// 1. Lần này là lần cuối thay đổi (mong vậy)
// 2. Usage trải dài lên mọi thứ -> Cho đầy đủ các tính chất cho AuctionList, Dashboard, RankingList(WIP)