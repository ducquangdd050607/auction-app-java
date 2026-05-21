package com.auctionapp.auctionappjava.server.service.trend;

import com.auctionapp.auctionappjava.common.model.BidTransaction;

import java.time.LocalDateTime;
import java.util.List;

public class BidVelocityAnalyzer implements AuctionActivityAnalyzer {
    private static final int RECENT_WINDOW_SECONDS = 60;

    @Override
    public TrendSignal analyze(AuctionTrendContext context) {
        List<BidTransaction> bids = context.bids();
        if (bids.isEmpty()) {
            return TrendSignal.none("chưa có bid");
        }

        LocalDateTime cutoff = context.now().minusSeconds(RECENT_WINDOW_SECONDS);
        long recentBids = bids.stream()
                .filter(bid -> !bid.getCreatedAt().isBefore(cutoff))
                .count();

        if (recentBids >= 5) {
            return new TrendSignal(35, "Đang nóng", recentBids + " bid trong 60 giây gần nhất");
        }
        if (recentBids >= 2) {
            return new TrendSignal(18, "Tăng tốc", recentBids + " bid trong 60 giây gần nhất");
        }
        return new TrendSignal(5, "Có hoạt động", "có bid nhưng chưa bùng nổ");
    }
}
