package com.auctionapp.auctionappjava.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
//cấu hình cho phép user tự động trả giá thay vì phải bấm tay
public class AutoBidConfig extends BaseEntity {
    private UUID auctionId;
    private UUID bidderId;
    private BigDecimal maxBid;
    private BigDecimal incrementAmount;
    private boolean enabled;

    public AutoBidConfig() {
        super();
        this.enabled = true;
    }

    public AutoBidConfig(UUID id,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt,
                         UUID auctionId,
                         UUID bidderId,
                         BigDecimal maxBid,
                         BigDecimal incrementAmount,
                         boolean enabled) {
        super(id, createdAt, updatedAt);
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.incrementAmount = incrementAmount;
        this.enabled = enabled;
    }

    public UUID getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(UUID auctionId) {
        this.auctionId = auctionId;
    }

    public UUID getBidderId() {
        return bidderId;
    }

    public void setBidderId(UUID bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(BigDecimal maxBid) {
        this.maxBid = maxBid;
    }

    public BigDecimal getIncrementAmount() {
        return incrementAmount;
    }

    public void setIncrementAmount(BigDecimal incrementAmount) {
        this.incrementAmount = incrementAmount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
