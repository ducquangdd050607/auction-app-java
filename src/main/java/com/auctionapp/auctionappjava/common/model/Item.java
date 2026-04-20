package com.auctionapp.auctionappjava.common.model;




import com.auctionapp.auctionappjava.common.model.BaseEntity;
import com.auctionapp.auctionappjava.common.model.BidTransaction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Item extends BaseEntity implements Serializable {
    private UUID ownerId;                 // thay cho seller trực tiếp
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String category;             // ART / ELECTRONICS / VEHICLE ...
    private String attributeOne;         // artist / brand / manufacturer
    private String attributeTwo;         // medium / model / registrationHint

    private UUID currentBidderId;        // thay cho bidder object
    private List<BidTransaction> bidHistory;

    public Item() {
        super();
        this.startingPrice = BigDecimal.ZERO;
        this.currentPrice = BigDecimal.ZERO;
        this.bidHistory = new ArrayList<>();
    }

    public Item(UUID id,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                UUID ownerId,
                String title,
                String description,
                BigDecimal startingPrice,
                BigDecimal currentPrice,
                LocalDateTime startTime,
                LocalDateTime endTime,
                String category,
                String attributeOne,
                String attributeTwo,
                UUID currentBidderId) {
        super(id, createdAt, updatedAt);
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.attributeOne = attributeOne;
        this.attributeTwo = attributeTwo;
        this.currentBidderId = currentBidderId;
        this.bidHistory = new ArrayList<>();
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAttributeOne() {
        return attributeOne;
    }

    public void setAttributeOne(String attributeOne) {
        this.attributeOne = attributeOne;
    }

    public String getAttributeTwo() {
        return attributeTwo;
    }

    public void setAttributeTwo(String attributeTwo) {
        this.attributeTwo = attributeTwo;
    }

    public UUID getCurrentBidderId() {
        return currentBidderId;
    }

    public void setCurrentBidderId(UUID currentBidderId) {
        this.currentBidderId = currentBidderId;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<BidTransaction> bidHistory) {
        this.bidHistory = bidHistory;
    }

    public boolean hasStarted(LocalDateTime now) {
        return startTime != null && !now.isBefore(startTime);
    }

    public boolean hasEnded(LocalDateTime now) {
        return endTime != null && !now.isBefore(endTime);
    }

    public boolean isOpen(LocalDateTime now) {
        return hasStarted(now) && !hasEnded(now);
    }

    public boolean placeBid(UUID bidderId, BigDecimal bidAmount) {
        if (bidderId == null || bidAmount == null) return false;
        if (!isOpen(LocalDateTime.now())) return false;
        if (currentPrice == null) currentPrice = startingPrice;

        BigDecimal minimumNextBid = currentPrice;
        if (bidAmount.compareTo(minimumNextBid) <= 0) return false;

        this.currentPrice = bidAmount;
        this.currentBidderId = bidderId;

        BidTransaction tx = new BidTransaction();
        tx.setAuctionId(this.getId()); // tạm dùng itemId như auctionId nếu chưa tách Auction
        tx.setBidderId(bidderId);
        tx.setAmount(bidAmount);
        tx.setAutoGenerated(false);
        tx.setNote("Manual bid on item");
        bidHistory.add(tx);

        touch();
        return true;
    }

    public String getDisplayMeta() {
        List<String> parts = new ArrayList<>();
        if (attributeOne != null && !attributeOne.isBlank()) parts.add(attributeOne);
        if (attributeTwo != null && !attributeTwo.isBlank()) parts.add(attributeTwo);
        return String.join(" | ", parts);
    }
}