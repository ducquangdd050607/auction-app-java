package com.auctionapp.auctionappjava.common.strategy;

import com.auctionapp.auctionappjava.common.model.Auction;

import java.time.LocalDateTime;

public interface AuctionExtensionStrategy {
    boolean shouldExtend(Auction auction, LocalDateTime bidTime);

    LocalDateTime extendTo(Auction auction, LocalDateTime bidTime);
}
