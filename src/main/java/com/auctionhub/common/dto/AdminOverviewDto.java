package com.auctionhub.common.dto;

import java.io.Serializable;
import java.util.List;

public record AdminOverviewDto(long totalUsers,
                               long totalAuctions,
                               long runningAuctions,
                               long finishedAuctions,
                               List<UserSummaryDto> users,
                               List<AuctionSummaryDto> auctions) implements Serializable {
}
