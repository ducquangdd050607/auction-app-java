package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

public class AutoBidEngine {
    public Optional<AutoBidConfig> selectNextConfig(List<AutoBidConfig> configs,
                                                    BigDecimal currentPrice,
                                                    UUID excludedBidderId,
                                                    UUID currentLeaderId) {
        if (configs == null || configs.isEmpty() || currentPrice == null) {
            return Optional.empty();
        }

        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(
                Comparator
                        .comparing(AutoBidConfig::getMaxBid, Comparator.reverseOrder())
                        .thenComparing(AutoBidConfig::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AutoBidConfig::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        for (AutoBidConfig config : configs) {
            if (config == null || !config.isEnabled()) continue;
            if (excludedBidderId != null && excludedBidderId.equals(config.getBidderId())) continue;
            if (currentLeaderId != null && currentLeaderId.equals(config.getBidderId())) continue;
            if (config.getMaxBid() == null || config.getIncrementAmount() == null) continue;
            if (config.getMaxBid().compareTo(currentPrice) <= 0) continue;
            queue.offer(config);
        }

        return Optional.ofNullable(queue.poll());
    }

    public BigDecimal nextBidAmount(BigDecimal currentPrice, AutoBidConfig config) {
        BigDecimal candidate = currentPrice.add(config.getIncrementAmount());
        if (candidate.compareTo(config.getMaxBid()) > 0) {
            return config.getMaxBid();
        }
        return candidate;
    }
}
