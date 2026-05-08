package com.auctionapp.auctionappjava.common.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record PlaceBidRequest(
        UUID auctionId,    // ID của phiên đấu giá đang tham gia
        UUID userId,       // ID của người dùng đang bấm nút đặt giá
        BigDecimal amount  // Số tiền họ muốn đặt
) implements Serializable {}