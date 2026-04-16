package com.auctionhub.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record AutoBidRequest(UUID auctionId, BigDecimal maxBid, BigDecimal increment) implements Serializable {
}
