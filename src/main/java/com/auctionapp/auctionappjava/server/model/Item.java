package com.auctionapp.auctionappjava.server.model;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Item extends BaseEntity {
  private UUID sellerId;
  private String title;
  private String description;
  private BigDecimal startingPrice;
  private ItemType itemType;
  private String attributeOne; // là các đặc điểm của đồ vật
  private String attributeTwo; //
  private byte[] imageData;

  protected Item() {
    super();
  }

  protected Item(
      UUID id,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      UUID sellerId,
      String title,
      String description,
      BigDecimal startingPrice,
      ItemType itemType,
      String attributeOne,
      String attributeTwo,
      byte[] imageData) {
    super(id, createdAt, updatedAt);
    this.sellerId = sellerId;
    this.title = title;
    this.description = description;
    this.startingPrice = startingPrice;
    this.itemType = itemType;
    this.attributeOne = attributeOne;
    this.attributeTwo = attributeTwo;
    this.imageData = imageData;
  }

  public UUID getSellerId() {
    return sellerId;
  }

  /*public void setSellerId(UUID userId) {
      this.userId = userId;
  }*/

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

  public byte[] getImageData() {
    return imageData;
  }

  public void setImageData(byte[] imageData) {
    this.imageData = imageData;
  }
}
