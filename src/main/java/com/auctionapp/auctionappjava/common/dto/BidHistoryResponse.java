package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;

import java.io.Serializable;
import java.math.BigDecimal;

public record BidHistoryResponse(
        String bidderName,
        String auctionName,
        BigDecimal auctionStartPrice,
        BigDecimal amount,
        AuctionStatus auctionStatus,
        String biddedTime) implements Serializable {}

// Chỉnh sửa BHR này cho phù hợp