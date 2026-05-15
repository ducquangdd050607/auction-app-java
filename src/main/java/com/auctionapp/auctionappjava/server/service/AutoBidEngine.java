package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

public class AutoBidEngine {
    public record ProxyBidResult(AutoBidConfig winnerConfig, BigDecimal finalPrice) {
    }

    public Optional<ProxyBidResult> resolveProxyBid(List<AutoBidConfig> configs,
                                                    BigDecimal currentPrice,
                                                    BigDecimal minimumIncrement,
                                                    UUID currentLeaderId) {
        if (configs == null || configs.isEmpty() || currentPrice == null) {
            return Optional.empty();
        }

        List<AutoBidConfig> activeConfigs = new ArrayList<>();
        for (AutoBidConfig config : configs) {
            if (config == null || !config.isEnabled()) continue;
            if (config.getMaxBid() == null || config.getIncrementAmount() == null) continue;
            if (config.getMaxBid().compareTo(currentPrice) <= 0) continue;
            activeConfigs.add(config);
        }

        if (activeConfigs.isEmpty()) {
            return Optional.empty();
        }

        activeConfigs.sort(Comparator
                .comparing(AutoBidConfig::getMaxBid, Comparator.reverseOrder())
                .thenComparing(AutoBidConfig::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AutoBidConfig::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        AutoBidConfig winner = activeConfigs.get(0);
        AutoBidConfig runnerUp = activeConfigs.size() > 1 ? activeConfigs.get(1) : null;

        // Proxy auto-bid: top1 wins, price follows top2 maxBid plus top1 bidStep.
        // If there is only one auto-bidder, increase from currentPrice by top1 bidStep.
        BigDecimal newBid = runnerUp == null
                ? currentPrice.add(winner.getIncrementAmount())
                : runnerUp.getMaxBid().add(winner.getIncrementAmount());

        if (newBid.compareTo(winner.getMaxBid()) > 0) {
            newBid = winner.getMaxBid();
        }
        if (newBid.compareTo(currentPrice) <= 0) {
            return Optional.empty();
        }
        return Optional.of(new ProxyBidResult(winner, newBid));
    }

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
