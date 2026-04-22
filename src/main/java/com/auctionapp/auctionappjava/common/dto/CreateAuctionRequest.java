package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.auctionapp.auctionappjava.common.enums.ItemType;

public record CreateAuctionRequest(
        UUID sellerId,
        String title,
        String description,
        BigDecimal startingPrice,
        BigDecimal minimumIncrement,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ItemType itemType,
        String attributeOne,
        String attributeTwo
) implements Serializable {}