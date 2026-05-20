package com.auctionapp.auctionappjava.common.strategy;

import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.model.AutoBidConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AutoBidEngine {

    public Optional<AutoBidResult> calculateNextBid(List<AutoBidConfig> configs,
                                                    BigDecimal defaultIncrement,
                                                    BigDecimal currentPrice,
                                                    UUID currentLeaderId) {
        if (defaultIncrement == null || defaultIncrement.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Default increment must be greater than zero");
        }

        List<BidCandidate> bidList = new ArrayList<>();

        for (AutoBidConfig config : getValidAutoBidConfigs(configs)) {
            bidList.add(new BidCandidate(
                    config.getAuctionId(),
                    config.getBidderId(),
                    config.getMaxBid(),
                    getIncrement(config, defaultIncrement),
                    config.getCreatedAt(),
                    true
            ));
        }

        // THEM AUTO-BID MANUAL: nguoi khong bat auto-bid van duoc dua vao list de so sanh.
        // maxBid cua nguoi manual = gia ho vua dat, increment = buoc gia co dinh cua san pham.
        if (currentPrice != null && currentLeaderId != null) {
            bidList.add(new BidCandidate(
                    null,
                    currentLeaderId,
                    currentPrice,
                    defaultIncrement,
                    LocalDateTime.now(),
                    false
            ));
        }

        if (bidList.size() < 2) {
            return Optional.empty();
        }

        bidList.sort(Comparator
                .comparing(BidCandidate::getMaxBid, Comparator.reverseOrder())
                .thenComparing(BidCandidate::getCreatedAt));

        BidCandidate firstBidder = bidList.get(0);
        BidCandidate secondBidder = bidList.get(1);

        // Neu nguoi maxBid cao nhat la manual thi khong co auto-bid nao vuot duoc.
        if (!firstBidder.isAutoBid()) {
            return Optional.empty();
        }

        // Neu nguoi auto-bid max cao nhat dang dan dau san roi thi khong can tao bid moi.
        if (firstBidder.getBidderId().equals(currentLeaderId)) {
            Optional<BidCandidate> tiedCompetitor = findTiedAutoBidCompetitor(
                    bidList,
                    firstBidder,
                    currentLeaderId
            );
            if (tiedCompetitor.isPresent() && currentPrice != null) {
                BidCandidate competitor = tiedCompetitor.get();
                BigDecimal bidAmount = competitor.getMaxBid();

                if (bidAmount.compareTo(currentPrice) > 0) {
                    return Optional.of(new AutoBidResult(
                            competitor.getAuctionId(),
                            competitor.getBidderId(),
                            bidAmount,
                            competitor.getMaxBid(),
                            currentPrice
                    ));
                }
            }
            return Optional.empty();
        }

        BigDecimal bidAmount = secondBidder.getMaxBid().add(firstBidder.getIncrement());
        if (bidAmount.compareTo(firstBidder.getMaxBid()) > 0) {
            bidAmount = firstBidder.getMaxBid();
        }

        if (currentPrice != null && bidAmount.compareTo(currentPrice) <= 0) {
            return Optional.empty();
        }

        return Optional.of(new AutoBidResult(
                firstBidder.getAuctionId(),
                firstBidder.getBidderId(),
                bidAmount,
                firstBidder.getMaxBid(),
                secondBidder.getMaxBid()
        ));
    }

    private List<AutoBidConfig> getValidAutoBidConfigs(List<AutoBidConfig> configs) {
        List<AutoBidConfig> result = new ArrayList<>();

        if (configs == null || configs.isEmpty()) {
            return result;
        }

        for (AutoBidConfig config : configs) {
            if (config == null) {
                continue;
            }

            if (!config.isEnabled()) {
                continue;
            }

            if (config.getAuctionId() == null || config.getBidderId() == null || config.getMaxBid() == null) {
                continue;
            }

            result.add(config);
        }

        return result;
    }

    private Optional<BidCandidate> findTiedAutoBidCompetitor(List<BidCandidate> bidList,
                                                             BidCandidate currentLeader,
                                                             UUID currentLeaderId) {
        for (BidCandidate candidate : bidList) {
            if (!candidate.isAutoBid()) {
                continue;
            }

            if (candidate.getBidderId().equals(currentLeaderId)) {
                continue;
            }

            if (candidate.getMaxBid().compareTo(currentLeader.getMaxBid()) == 0) {
                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    private BigDecimal getIncrement(AutoBidConfig config, BigDecimal defaultIncrement) {
        if (config.getIncrementAmount() == null) {
            return defaultIncrement;
        }
        return config.getIncrementAmount();
    }

    private static class BidCandidate {
        private UUID auctionId;
        private UUID bidderId;
        private BigDecimal maxBid;
        private BigDecimal increment;
        private LocalDateTime createdAt;
        private boolean autoBid;

        public BidCandidate(UUID auctionId,
                            UUID bidderId,
                            BigDecimal maxBid,
                            BigDecimal increment,
                            LocalDateTime createdAt,
                            boolean autoBid) {
            this.auctionId = auctionId;
            this.bidderId = bidderId;
            this.maxBid = maxBid;
            this.increment = increment;
            this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
            this.autoBid = autoBid;
        }

        public UUID getAuctionId() {
            return auctionId;
        }

        public UUID getBidderId() {
            return bidderId;
        }

        public BigDecimal getMaxBid() {
            return maxBid;
        }

        public BigDecimal getIncrement() {
            return increment;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public boolean isAutoBid() {
            return autoBid;
        }
    }

    public static class AutoBidResult {
        private UUID auctionId;
        private UUID bidderId;
        private BigDecimal bidAmount;
        private BigDecimal firstMaxBid;
        private BigDecimal secondMaxBid;

        public AutoBidResult(UUID auctionId,
                             UUID bidderId,
                             BigDecimal bidAmount,
                             BigDecimal firstMaxBid,
                             BigDecimal secondMaxBid) {
            this.auctionId = auctionId;
            this.bidderId = bidderId;
            this.bidAmount = bidAmount;
            this.firstMaxBid = firstMaxBid;
            this.secondMaxBid = secondMaxBid;
        }

        public UUID getAuctionId() {
            return auctionId;
        }

        public UUID getBidderId() {
            return bidderId;
        }

        public BigDecimal getBidAmount() {
            return bidAmount;
        }

        public BigDecimal getFirstMaxBid() {
            return firstMaxBid;
        }

        public BigDecimal getSecondMaxBid() {
            return secondMaxBid;
        }
    }
}
