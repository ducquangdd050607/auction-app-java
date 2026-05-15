package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public class Auction extends BaseEntity {
    private UUID itemId;
    private UUID sellerId;
    private BigDecimal currentPrice;
    private UUID leadingBidderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private BigDecimal minimumIncrement;
    private UUID winnerId;
    private int biddersCount;

    public Auction() {
        super();
    }

    public Auction(UUID id,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt,
                   UUID itemId,
                   UUID sellerId,
                   BigDecimal currentPrice,
                   UUID leadingBidderId,
                   LocalDateTime startTime,
                   LocalDateTime endTime,
                   AuctionStatus status,
                   BigDecimal minimumIncrement,
                   UUID winnerId) {
        super(id, createdAt, updatedAt);
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.currentPrice = currentPrice;
        this.leadingBidderId = leadingBidderId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.minimumIncrement = minimumIncrement;
        this.winnerId = winnerId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public UUID getLeadingBidderId() {
        return leadingBidderId;
    }

    public void setLeadingBidderId(UUID leadingBidderId) {
        this.leadingBidderId = leadingBidderId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public BigDecimal getMinimumIncrement() {
        return minimumIncrement;
    }

    public void setMinimumIncrement(BigDecimal minimumIncrement) {
        this.minimumIncrement = minimumIncrement;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public boolean hasStarted(LocalDateTime now) {
        return !now.isBefore(startTime);
    }

    public boolean hasEnded(LocalDateTime now) {
        return !now.isBefore(endTime);
    }

    public boolean isAcceptingBids(LocalDateTime now) {
        return status == AuctionStatus.RUNNING && hasStarted(now) && !hasEnded(now);
    }

    public int getBiddersCount() {
        return biddersCount;
    }

    public void setBiddersCount(int biddersCount) {
        this.biddersCount = biddersCount;
    }
}
