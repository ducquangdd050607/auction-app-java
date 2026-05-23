package com.auctionapp.auctionappjava.server.service.trend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

public class TimePressureAnalyzer implements AuctionActivityAnalyzer {

    @Override
    public TrendSignal analyze(AuctionTrendContext context) {
        long totalSeconds = Duration.between(
                context.auction().getStartTime(),
                context.auction().getEndTime()
        ).toSeconds();

        long secondsLeft = Duration.between(
                context.now(),
                context.auction().getEndTime()
        ).toSeconds();

        double timeLeftRatio = (double) secondsLeft / totalSeconds;

        // Time Pressure = 1.0 + 0.25 × (1 - timeLeftRatio)

        double pressure = 1.0 + 0.25 * (1.0 - timeLeftRatio);

        String label;
        if (timeLeftRatio >= 0.5) {
            label = "Chưa vội";
        }

        else if (timeLeftRatio >= 0.1) {
            label = "Sắp hết";
        }

        else {
            label = "Vội";
        }

        String reason = "Còn " + String.format("%.1f", timeLeftRatio * 100) + "% thời gian";

        return new TrendSignal(
                BigDecimal.valueOf(pressure).setScale(4, RoundingMode.HALF_UP),
                BigDecimal.ONE,
                label,
                reason
        );
    }
}