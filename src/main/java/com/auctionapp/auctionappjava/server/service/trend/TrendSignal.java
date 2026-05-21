package com.auctionapp.auctionappjava.server.service.trend;

public record TrendSignal(int score, String label, String reason) {
    public static TrendSignal none(String reason) {
        return new TrendSignal(0, "Bình thường", reason);
    }
}
