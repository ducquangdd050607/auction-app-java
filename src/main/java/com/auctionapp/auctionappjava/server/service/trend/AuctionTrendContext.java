package com.auctionapp.auctionappjava.server.service.trend;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.BidTransaction;

import java.time.LocalDateTime;
import java.util.List;

public record AuctionTrendContext(
        Auction auction,
        AuctionSummaryResponse summary,
        List<BidTransaction> bids,
        int botsCounter,
        LocalDateTime now
) {
}
