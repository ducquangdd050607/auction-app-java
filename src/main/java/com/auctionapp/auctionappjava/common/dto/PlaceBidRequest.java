package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record PlaceBidRequest(UUID auctionId, BigDecimal amount) implements Serializable {
}
