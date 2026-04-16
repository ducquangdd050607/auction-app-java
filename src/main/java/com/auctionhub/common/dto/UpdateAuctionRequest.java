package com.auctionhub.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateAuctionRequest(UUID auctionId,
                                   String title,
                                   String description,
                                   BigDecimal startingPrice,
                                   BigDecimal minimumIncrement,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime,
                                   String attributeOne,
                                   String attributeTwo) implements Serializable {
}
