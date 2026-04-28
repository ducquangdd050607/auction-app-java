package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record AuctionSummaryResponse(
        String auctionId,
        String itemName,
        String imagePath, // Đường dẫn ảnh thu nhỏ
        BigDecimal currentPrice,
        long endTimeMillis, // Truyền kiểu số long (timestamp) để dễ đếm ngược
        String status
) implements Serializable {}