package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class AuctionItem extends BaseEntity {
    private UUID sellerId;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private ItemType itemType;
    private String attributeOne;
    private String attributeTwo;

    protected AuctionItem() {
        super();
    }

    protected AuctionItem(UUID id,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          UUID sellerId,
                          String title,
                          String description,
                          BigDecimal startingPrice,
                          ItemType itemType,
                          String attributeOne,
                          String attributeTwo) {
        super(id, createdAt, updatedAt);
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.itemType = itemType;
        this.attributeOne = attributeOne;
        this.attributeTwo = attributeTwo;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
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

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
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

    public String getDisplayMeta() {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (attributeOne != null && !attributeOne.isBlank()) {
            parts.add(attributeOne);
        }
        if (attributeTwo != null && !attributeTwo.isBlank()) {
            parts.add(attributeTwo);
        }
        return String.join(" | ", parts);
    }
}
