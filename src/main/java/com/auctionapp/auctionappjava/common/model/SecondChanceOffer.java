package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.SecondChanceOfferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SecondChanceOffer extends BaseEntity {
    private UUID auctionId;
    private UUID originalWinnerId;
    private UUID offeredBidderId;
    private BigDecimal offerAmount;
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
    private SecondChanceOfferStatus status;
    private String note;

    public SecondChanceOffer() {
        super();
        this.status = SecondChanceOfferStatus.OFFERED;
    }

    public SecondChanceOffer(UUID id,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt,
                             UUID auctionId,
                             UUID originalWinnerId,
                             UUID offeredBidderId,
                             BigDecimal offerAmount,
                             LocalDateTime expiresAt,
                             LocalDateTime respondedAt,
                             SecondChanceOfferStatus status,
                             String note) {
        super(id, createdAt, updatedAt);
        this.auctionId = auctionId;
        this.originalWinnerId = originalWinnerId;
        this.offeredBidderId = offeredBidderId;
        this.offerAmount = offerAmount;
        this.expiresAt = expiresAt;
        this.respondedAt = respondedAt;
        this.status = status == null ? SecondChanceOfferStatus.OFFERED : status;
        this.note = note;
    }

    public UUID getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(UUID auctionId) {
        this.auctionId = auctionId;
    }

    public UUID getOriginalWinnerId() {
        return originalWinnerId;
    }

    public void setOriginalWinnerId(UUID originalWinnerId) {
        this.originalWinnerId = originalWinnerId;
    }

    public UUID getOfferedBidderId() {
        return offeredBidderId;
    }

    public void setOfferedBidderId(UUID offeredBidderId) {
        this.offeredBidderId = offeredBidderId;
    }

    public BigDecimal getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(BigDecimal offerAmount) {
        this.offerAmount = offerAmount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public SecondChanceOfferStatus getStatus() {
        return status;
    }

    public void setStatus(SecondChanceOfferStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isExpired(LocalDateTime now) {
        return status == SecondChanceOfferStatus.OFFERED && expiresAt != null && !now.isBefore(expiresAt);
    }
}
