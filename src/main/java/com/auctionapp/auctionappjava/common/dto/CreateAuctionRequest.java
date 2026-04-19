package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.ItemType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAuctionRequest(ItemType itemType,
                                   String title,
                                   String description,
                                   BigDecimal startingPrice,
                                   BigDecimal minimumIncrement,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime,
                                   String attributeOne,
                                   String attributeTwo) implements Serializable {
}
