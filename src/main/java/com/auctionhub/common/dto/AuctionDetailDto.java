package com.auctionhub.common.dto;

import java.io.Serializable;
import java.util.List;

public record AuctionDetailDto(AuctionSummaryDto summary,
                               String attributeOne,
                               String attributeTwo,
                               List<BidDto> bidHistory,
                               String winnerName,
                               boolean canCurrentUserBid,
                               String statusExplanation) implements Serializable {
}
