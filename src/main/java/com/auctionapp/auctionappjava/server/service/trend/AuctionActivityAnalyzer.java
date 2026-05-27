package com.auctionapp.auctionappjava.server.service.trend;

public interface AuctionActivityAnalyzer {
  TrendSignal analyze(AuctionTrendContext context);
}
