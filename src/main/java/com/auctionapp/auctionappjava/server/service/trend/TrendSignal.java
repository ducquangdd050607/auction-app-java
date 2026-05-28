package com.auctionapp.auctionappjava.server.service.trend;

import java.math.BigDecimal;

public record TrendSignal(BigDecimal value, BigDecimal coefficient, String label, String reason) {

  public static TrendSignal notEnoughData(String reason) {
    return new TrendSignal(BigDecimal.ZERO, BigDecimal.ONE, "Bình thường", reason);
  }
}
