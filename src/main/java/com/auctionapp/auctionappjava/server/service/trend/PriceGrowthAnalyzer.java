package com.auctionapp.auctionappjava.server.service.trend;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceGrowthAnalyzer implements AuctionActivityAnalyzer {
    @Override
    public TrendSignal analyze(AuctionTrendContext context) {
        BigDecimal startPrice = context.summary().startPrice();
        BigDecimal currentPrice = context.summary().currentPrice();
        if (startPrice == null || currentPrice == null || startPrice.signum() <= 0) {
            return TrendSignal.none("chưa đủ dữ liệu tăng giá");
        }

        BigDecimal growthPercent = currentPrice.subtract(startPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(startPrice, 0, RoundingMode.HALF_UP);

        int growth = growthPercent.intValue();
        if (growth >= 100) {
            return new TrendSignal(25, "Tăng mạnh", "giá tăng " + growth + "%");
        }
        if (growth >= 30) {
            return new TrendSignal(12, "Tăng giá", "giá tăng " + growth + "%");
        }
        return TrendSignal.none("chưa đủ dữ liệu tăng giá");
    }
}
