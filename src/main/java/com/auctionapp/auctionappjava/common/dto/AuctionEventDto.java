package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.EventType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionEventDto(
        EventType eventType,
        UUID auctionId,
        AuctionSummaryDto auction,
        BidDto bid,
        String message,
        LocalDateTime occurredAt
) implements Serializable {
}