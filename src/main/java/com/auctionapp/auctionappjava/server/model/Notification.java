package com.auctionapp.auctionappjava.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {
  private UUID id;
  private UUID userId;
  private UUID auctionId;
  private String type;
  private String message;
  private LocalDateTime createdAt;

  public Notification(
      UUID id, UUID userId, UUID auctionId, String type, String message, LocalDateTime createdAt) {
    this.id = id;
    this.userId = userId;
    this.auctionId = auctionId;
    this.type = type;
    this.message = message;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getAuctionId() {
    return auctionId;
  }

  public String getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
