package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.AuctionTrendResponse;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import com.auctionapp.auctionappjava.common.model.BidTransaction;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.AutoBidDao;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAutoBidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcBidDao;
import com.auctionapp.auctionappjava.server.service.trend.AuctionActivityAnalyzer;
import com.auctionapp.auctionappjava.server.service.trend.AuctionTrendContext;
import com.auctionapp.auctionappjava.server.service.trend.AutoBidPressureAnalyzer;
import com.auctionapp.auctionappjava.server.service.trend.BidVelocityAnalyzer;
import com.auctionapp.auctionappjava.server.service.trend.PriceGrowthAnalyzer;
import com.auctionapp.auctionappjava.server.service.trend.TimePressureAnalyzer;
import com.auctionapp.auctionappjava.server.service.trend.TrendSignal;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuctionTrendService {
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final AutoBidDao autoBidDao;
    private final List<AuctionActivityAnalyzer> analyzers;

    public AuctionTrendService() {
        this(new JdbcAuctionDao(), new JdbcBidDao(), new JdbcAutoBidDao(),
                List.of(
                        new BidVelocityAnalyzer(),
                        new AutoBidPressureAnalyzer(),
                        new TimePressureAnalyzer(),
                        new PriceGrowthAnalyzer()
                ));
    }

    AuctionTrendService(AuctionDao auctionDao,
                        BidDao bidDao,
                        AutoBidDao autoBidDao,
                        List<AuctionActivityAnalyzer> analyzers) {
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.autoBidDao = autoBidDao;
        this.analyzers = analyzers;
    }

    public Response handleGetAuctionTrends() {
        try {
            List<AuctionTrendResponse> trends = auctionDao.findAllSummaries().stream()
                    .map(this::buildTrend)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparingInt(AuctionTrendResponse::trendScore).reversed())
                    .collect(Collectors.toList());

            return new Response(true, "Tải xu hướng đấu giá thành công", trends);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi phân tích xu hướng: " + e.getMessage(), null);
        }
    }

    private Optional<AuctionTrendResponse> buildTrend(AuctionSummaryResponse summary) {
        Optional<Auction> auctionOpt = auctionDao.findById(java.util.UUID.fromString(summary.auctionId()));
        if (auctionOpt.isEmpty()) {
            return Optional.empty();
        }

        Auction auction = auctionOpt.get();
        List<BidTransaction> bids = bidDao.findByAuctionId(auction.getId());
        List<AutoBidConfig> autoBidConfigs = autoBidDao.findEnabledByAuctionId(auction.getId());
        AuctionTrendContext context = new AuctionTrendContext(
                auction,
                summary,
                bids,
                autoBidConfigs,
                LocalDateTime.now()
        );

        List<TrendSignal> signals = analyzers.stream()
                .map(analyzer -> analyzer.analyze(context))
                .toList();

        int score = signals.stream().mapToInt(TrendSignal::score).sum();
        String label = chooseLabel(score);
        String reason = signals.stream()
                .filter(signal -> signal.score() > 0)
                .map(TrendSignal::reason)
                .collect(Collectors.joining("; "));
        if (reason.isBlank()) {
            reason = "chưa có dữ liệu nổi bật";
        }

        return Optional.of(new AuctionTrendResponse(
                summary.auctionId(),
                summary.itemName(),
                summary.category(),
                summary.currentPrice(),
                summary.status(),
                summary.bidderCount(),
                bids.size(),
                score,
                label,
                reason
        ));
    }

    private String chooseLabel(int score) {
        if (score >= 70) {
            return "Rất nóng";
        }
        if (score >= 40) {
            return "Đang nóng";
        }
        if (score >= 20) {
            return "Đang tăng";
        }
        return "Bình thường";
    }
}
