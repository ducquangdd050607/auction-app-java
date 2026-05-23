package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.AuctionTrendResponse;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.BidTransaction;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.AutoBidDao;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAutoBidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcBidDao;
import com.auctionapp.auctionappjava.server.service.trend.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AuctionTrendService {

    private static final BigDecimal growthWeightValue = new BigDecimal("0.40");
    private static final BigDecimal freqWeightValue = new BigDecimal("0.35");
    private static final BigDecimal autobidWeightValue = new BigDecimal("0.25");

    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final AutoBidDao botsDao;

    private final FrequencyAnalyzer frequencyAnalyzer;
    private final AutoBiddersAnalyzer autoBiddersAnalyzer;
    private final PriceGrowthAnalyzer priceGrowthAnalyzer;
    private final TimePressureAnalyzer timePressureAnalyzer;

    public AuctionTrendService() {
        this.auctionDao = new JdbcAuctionDao();
        this.bidDao = new JdbcBidDao();
        this.botsDao = new JdbcAutoBidDao();

        this.frequencyAnalyzer = new FrequencyAnalyzer(this.bidDao);
        this.priceGrowthAnalyzer = new PriceGrowthAnalyzer();
        this.autoBiddersAnalyzer = new AutoBiddersAnalyzer();
        this.timePressureAnalyzer = new TimePressureAnalyzer();
    }

    public Response handleGetAuctionTrends() {
        try {
            List<AuctionTrendResponse> trends = auctionDao.findRunningAuctionSummaries().stream()
                    .map(this::buildTrend)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(
                            AuctionTrendResponse::trendScore,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ))
                    .collect(Collectors.toList());

            return new Response(true, "Tải xu hướng đấu giá thành công", trends);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi phân tích xu hướng: " + e.getMessage(), null);
        }
    }

    private Optional<AuctionTrendResponse> buildTrend(AuctionSummaryResponse summary) {

        Optional<Auction> auctionOpt = auctionDao.findById(UUID.fromString(summary.auctionId()));
        if (auctionOpt.isEmpty()) {
            return Optional.empty();
        }

        Auction auction = auctionOpt.get();

        if (bidDao.countBiddersByAuctionId(auction.getId()) == 0) {
            return Optional.empty();
        }

        List<BidTransaction> bids = bidDao.findByAuctionId(auction.getId());
        int bots = botsDao.countBotsByAuctionId(UUID.fromString(summary.auctionId()));

        AuctionTrendContext context = new AuctionTrendContext(
                auction,
                summary,
                bids,
                bots,
                LocalDateTime.now()
        );

        TrendSignal freqSignal = frequencyAnalyzer.analyze(context);
        TrendSignal autoBidSignal = autoBiddersAnalyzer.analyze(context);
        TrendSignal growthSignal = priceGrowthAnalyzer.analyze(context);
        TrendSignal timeSignal = timePressureAnalyzer.analyze(context);

        BigDecimal growthVal = growthSignal.value();
        BigDecimal freqVal = freqSignal.value();
        BigDecimal autoBidVal = autoBidSignal.value();
        BigDecimal timePressureMultiplier = timeSignal.value();

        BigDecimal freqCoeff = freqSignal.coefficient();
        BigDecimal autoBidCoeff = autoBidSignal.coefficient();
        BigDecimal growthCoeff = growthSignal.coefficient();

        // BaseScore = (0.40 * Growth * GC + 0.35 * Freq * FC + 0.25 * AutoBid * AtBC)
        BigDecimal baseScore = growthVal.multiply(growthWeightValue).multiply(growthCoeff)
                .add(freqVal.multiply(freqWeightValue).multiply(freqCoeff))
                .add(autoBidVal.multiply(autobidWeightValue).multiply(autoBidCoeff));

        // FinalScore = BaseScore * TimePressure / (sigma(Weight * Coefficient))
        BigDecimal finalScore = baseScore.multiply(timePressureMultiplier)

                .divide( (growthWeightValue.multiply(growthCoeff)
                        .add(freqWeightValue.multiply(freqCoeff))
                        .add(autobidWeightValue.multiply(autoBidCoeff))),4, RoundingMode.HALF_UP)

                .setScale(4, RoundingMode.HALF_UP);
        // Debug
        System.out.printf("[Trend] %s | G=%.4f F=%.4f A=%.4f TP=%.4f → Score=%.4f%n",
                summary.auctionId(),
                growthVal.doubleValue(), freqVal.doubleValue(),
                autoBidVal.doubleValue(), timePressureMultiplier.doubleValue(),
                finalScore.doubleValue());

        // Gom reason hiển thị UI
        String finalReason = List.of(autoBidSignal, freqSignal, growthSignal, timeSignal)
                .stream()
                .map(TrendSignal::reason)
                .filter(r -> r != null && !r.isBlank())
                .collect(Collectors.joining("; "));

        if (finalReason.isBlank()) finalReason = "Chưa có dữ liệu nổi bật";

        return Optional.of(new AuctionTrendResponse(
                summary.auctionId(),
                summary.itemName(),
                summary.category(),
                summary.currentPrice(),
                summary.status(),
                summary.bidderCount(),
                bids.size(),
                finalScore,
                null,
                finalReason
        ));
    }

    public Response handleGetMostTrendingAuction() {
        try {
            Response allTrends = handleGetAuctionTrends();

            List<AuctionTrendResponse> trends = (List<AuctionTrendResponse>) allTrends.data();

            if (trends == null || trends.isEmpty()) {
                return new Response(false, "Không có phiên đấu giá nào đang chạy", null);
            }

            AuctionTrendResponse top = trends.get(0);

            return new Response(true, "Phiên đấu giá xu hướng nhất", top);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi khi xác định phiên xu hướng: " + e.getMessage(), null);
        }
    }

}