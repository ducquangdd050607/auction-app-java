package com.auctionapp.auctionappjava.server.service.trend;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AutoBiddersAnalyzer implements AuctionActivityAnalyzer {

  @Override
  public TrendSignal analyze(AuctionTrendContext context) {

    int autobidEnabledCount = context.botsCounter();
    int bidderCount = context.summary().bidderCount();

    if (autobidEnabledCount == 0) {
      // Phiên sạch: không có auto-bid
      return new TrendSignal(
          new BigDecimal("1.0"), BigDecimal.ONE, "Phiên sạch", "Không có auto-bid");
    }
    // Xác định mức độ bots trong phiên:
    // Số bots trong phiên(lấy từ auto_bid_config) / Số bidders

    BigDecimal ratio =
        new BigDecimal(autobidEnabledCount)
            .divide(new BigDecimal(bidderCount), 4, RoundingMode.HALF_UP);

    if (ratio.compareTo(new BigDecimal("0.5")) < 0) {

      // Có auto-bid ở mức bình thường(<50%)
      return new TrendSignal(
          ratio, new BigDecimal("0.8"), "Có auto-bid", autobidEnabledCount + " auto-bid đang bật");
    } else {
      // Bot war: phạt điểm thấp(>50%)
      return new TrendSignal(
          ratio, new BigDecimal("0.5"), "Bot war", autobidEnabledCount + " auto-bid đang bật");
    }
  }
}
