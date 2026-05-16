package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.PaymentDeadlineStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDeadline extends BaseEntity {
    private UUID auctionId;
    private UUID winnerId;
    private BigDecimal amountDue;
    private LocalDateTime deadlineAt;
    private LocalDateTime paidAt;
    private LocalDateTime failedAt;
    private PaymentDeadlineStatus status;
    private String note;

    public PaymentDeadline() {
        super();
        this.status = PaymentDeadlineStatus.PENDING;
    }

    public PaymentDeadline(UUID id,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           UUID auctionId,
                           UUID winnerId,
                           BigDecimal amountDue,
                           LocalDateTime deadlineAt,
                           LocalDateTime paidAt,
                           LocalDateTime failedAt,
                           PaymentDeadlineStatus status,
                           String note) {
        super(id, createdAt, updatedAt);
        this.auctionId = auctionId;
        this.winnerId = winnerId;
        this.amountDue = amountDue;
        this.deadlineAt = deadlineAt;
        this.paidAt = paidAt;
        this.failedAt = failedAt;
        this.status = status == null ? PaymentDeadlineStatus.PENDING : status;
        this.note = note;
    }

    public UUID getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(UUID auctionId) {
        this.auctionId = auctionId;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public LocalDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(LocalDateTime deadlineAt) {
        this.deadlineAt = deadlineAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public PaymentDeadlineStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentDeadlineStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isExpired(LocalDateTime now) {
        return status == PaymentDeadlineStatus.PENDING && deadlineAt != null && !now.isBefore(deadlineAt);
    }
}
