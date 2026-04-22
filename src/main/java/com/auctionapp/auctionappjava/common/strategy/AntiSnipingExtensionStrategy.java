package com.auctionapp.auctionappjava.common.strategy;

import com.auctionapp.auctionappjava.common.model.Auction;

import java.time.LocalDateTime;

public class AntiSnipingExtensionStrategy implements AuctionExtensionStrategy {
    private final long thresholdSeconds;// thời gian nguy hieemr <= 36s

    private final long extensionSeconds; // tg gia hạn

    public AntiSnipingExtensionStrategy(long thresholdSeconds, long extensionSeconds) {
        this.thresholdSeconds = thresholdSeconds; // gia hạn
        this.extensionSeconds = extensionSeconds;
    }

    @Override
    public boolean shouldExtend(Auction auction, LocalDateTime bidTime) {
        long secondsRemaining = java.time.Duration.between(bidTime, auction.getEndTime()).getSeconds();
        return secondsRemaining >= 0 && secondsRemaining <= thresholdSeconds;
    }

    @Override
    public LocalDateTime extendTo(Auction auction, LocalDateTime bidTime) {
        if (!shouldExtend(auction, bidTime)) {
            return auction.getEndTime();
        }
        return auction.getEndTime().plusSeconds(extensionSeconds);
    }
}
