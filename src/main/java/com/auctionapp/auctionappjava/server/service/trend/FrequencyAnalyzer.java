package com.auctionapp.auctionappjava.server.service.trend;

import com.auctionapp.auctionappjava.server.dao.BidDao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class FrequencyAnalyzer implements AuctionActivityAnalyzer {

    private final BidDao bidDao;

    public FrequencyAnalyzer(BidDao bidDao) {
        this.bidDao = bidDao;
    }

    @Override
    public TrendSignal analyze(AuctionTrendContext context) {

        UUID auctionId = context.auction().getId();

        LocalDateTime startTime = context.auction().getStartTime();
        LocalDateTime endTime = context.auction().getEndTime();

        long timeGapSeconds = Duration.between(startTime, endTime)
                .dividedBy(10)
                .toSeconds();

        LocalDateTime now = context.now();
        LocalDateTime cutoff = now.minusSeconds(timeGapSeconds);
        LocalDateTime previousCutoff = cutoff.minusSeconds(timeGapSeconds);

        long recentBids = bidDao.countBidsInWindowTime(auctionId, cutoff, now);
        long lastRecentBids = bidDao.countBidsInWindowTime(auctionId, previousCutoff, cutoff);

        if (lastRecentBids == 0) {
            // Đợt tăng tốc đầu tiên → giá trị = 1.0
            return new TrendSignal(
                    BigDecimal.ONE,
                    BigDecimal.ONE,
                    "Tăng tốc",
                    recentBids + " bid trong thời gian gần đây"
            );
        }

        BigDecimal ratio = BigDecimal.valueOf(recentBids)
                .divide(BigDecimal.valueOf(lastRecentBids), 4, RoundingMode.HALF_UP);

        // Cap ratio ở 2.0 để tránh phá vỡ thang điểm
        BigDecimal cappedRatio = ratio.min(new BigDecimal("2.0"));

        String status;
        BigDecimal freqCoeff;

        if (recentBids > lastRecentBids) {
            status = "Tăng tốc";
            freqCoeff = new BigDecimal("1.5");
        } else if (recentBids < lastRecentBids) {
            status = "Giảm tốc";
            freqCoeff = new BigDecimal("0.8");
        } else {
            status = "Không đổi";
            freqCoeff = BigDecimal.ONE;
        }

        return new TrendSignal(
                cappedRatio,
                freqCoeff,
                status,
                recentBids + " bid trong thời gian gần đây"
        );
    }
}