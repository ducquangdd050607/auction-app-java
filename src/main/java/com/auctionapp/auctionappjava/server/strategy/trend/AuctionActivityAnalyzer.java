package com.auctionapp.auctionappjava.server.strategy.trend;

public interface AuctionActivityAnalyzer {
  TrendSignal analyze(AuctionTrendContext context);
}
