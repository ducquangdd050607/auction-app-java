package com.auctionapp.auctionappjava.server.service.postauction;

import com.auctionapp.auctionappjava.common.model.Auction;

import java.time.Duration;
import java.time.LocalDateTime;

public class FixedDurationDeadlinePolicy implements DeadlinePolicy {
    private final Duration duration;

    public FixedDurationDeadlinePolicy(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Payment duration must be positive");
        }
        this.duration = duration;
    }

    @Override
    public LocalDateTime calculateDeadline(Auction auction, LocalDateTime now) {
        return now.plus(duration);
    }
}
