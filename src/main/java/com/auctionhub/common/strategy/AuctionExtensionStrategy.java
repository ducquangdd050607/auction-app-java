package com.auctionhub.common.strategy;

import com.auctionhub.common.model.Auction;

import java.time.LocalDateTime;

public interface AuctionExtensionStrategy {
    boolean shouldExtend(Auction auction, LocalDateTime bidTime);

    LocalDateTime extendTo(Auction auction, LocalDateTime bidTime);
}
