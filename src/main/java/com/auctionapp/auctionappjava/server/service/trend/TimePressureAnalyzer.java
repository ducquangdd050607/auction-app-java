package com.auctionapp.auctionappjava.server.service.trend;

import com.auctionapp.auctionappjava.common.model.BidTransaction;

import java.time.Duration;

public class TimePressureAnalyzer implements AuctionActivityAnalyzer {
    @Override
    public TrendSignal analyze(AuctionTrendContext context) {
        long minutesLeft = Duration.between(context.now(), context.auction().getEndTime()).toMinutes();
        if (minutesLeft > 10 || minutesLeft < 0) {
            return TrendSignal.none("chưa có áp lực thời gian");
        }

        long lateBids = context.bids().stream()
                .filter(bid -> isNearEnd(context, bid))
                .count();

        if (minutesLeft <= 5 && lateBids >= 2) {
            return new TrendSignal(30, "FOMO", lateBids + " bid sát giờ kết thúc");
        }
        if (minutesLeft <= 10) {
            return new TrendSignal(12, "Sắp kết thúc", "còn " + Math.max(minutesLeft, 0) + " phút");
        }
        return TrendSignal.none("chưa có áp lực thời gian");
    }

    private boolean isNearEnd(AuctionTrendContext context, BidTransaction bid) {
        long minutesBeforeEnd = Duration.between(bid.getCreatedAt(), context.auction().getEndTime()).toMinutes();
        return minutesBeforeEnd >= 0 && minutesBeforeEnd <= 10;
    }
}
