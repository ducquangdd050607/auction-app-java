package com.auctionapp.auctionappjava.server.service.trend;

public class AutoBidPressureAnalyzer implements AuctionActivityAnalyzer {
    @Override
    public TrendSignal analyze(AuctionTrendContext context) {
        int enabledAutoBids = context.autoBidConfigs().size();
        if (enabledAutoBids >= 3) {
            return new TrendSignal(25, "Bot war", enabledAutoBids + " auto-bid đang bật");
        }
        if (enabledAutoBids >= 1) {
            return new TrendSignal(12, "Có auto-bid", enabledAutoBids + " auto-bid đang bật");
        }
        return TrendSignal.none("không có auto-bid");
    }
}
