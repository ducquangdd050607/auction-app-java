package com.auctionapp.auctionappjava.server.service.trend;

import com.auctionapp.auctionappjava.common.model.BidTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PriceGrowthAnalyzer implements AuctionActivityAnalyzer {

    @Override
    public TrendSignal analyze(AuctionTrendContext context) {

        List<BidTransaction> transactions = context.bids();

        // Trường hợp chỉ mới có 1 lượt đặt đầu

        if (transactions.size() < 2) {
            return new TrendSignal(
                    BigDecimal.ONE,
                    BigDecimal.ONE,
                    "Bình thường",
                    "Chưa có biến động giá");
        }

        // Xác định 2 lần đặt gần nhất

        BidTransaction latestBid = transactions.get(transactions.size() - 1);
        BidTransaction previousBid = transactions.get(transactions.size() - 2);

        BigDecimal currentPrice = latestBid.getAmount();
        BigDecimal previousPrice = previousBid.getAmount();


        // Tính độ tăng của 2 lần đặt:
        // (Giá hiện tại - Giá trước) / Giá trước


        BigDecimal growthPercent = currentPrice.subtract(previousPrice)
                .divide(previousPrice, 4, RoundingMode.HALF_UP);
        double growth = growthPercent.doubleValue();

        BigDecimal assignedValue;
        BigDecimal priceCoefficient;
        String status;

        if (growth <= 0.05) {
            assignedValue = new BigDecimal("0.5");
            priceCoefficient = new BigDecimal("0.8");
            status = "Tăng chậm";

        } else if (growth <= 0.2) {
            assignedValue = new BigDecimal("1.0");
            priceCoefficient = BigDecimal.ONE;
            status = "Tăng đều";

        } else if (growth <= 1) {
            assignedValue = new BigDecimal("1.5");
            priceCoefficient = new BigDecimal("1.2");
            status = "Bứt phá";

        } else {
            assignedValue = new BigDecimal("2.0");
            priceCoefficient = new BigDecimal("1.5");
            status = "Tăng đột biến";

        }

        BigDecimal displayPercent = growthPercent
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);

        return new TrendSignal(
                assignedValue,
                priceCoefficient,
                status,
                "Lượt đặt cuối tăng " + displayPercent + "% so với lượt trước"
        );
    }
}