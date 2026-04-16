package com.auctionhub.common.dto;

import java.io.Serializable;
import java.util.UUID;

public record AuctionIdRequest(UUID auctionId) implements Serializable {
}
