package com.auctionapp.auctionappjava.server.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Wallet extends BaseEntity {

  private UUID userId;
  private BigDecimal balance = BigDecimal.ZERO;

  public Wallet() {
    super();
  }

  public Wallet(
      UUID id, LocalDateTime createdAt, LocalDateTime updatedAt, UUID userId, BigDecimal balance) {
    super(id, createdAt, updatedAt);
    this.userId = userId;
    this.balance = balance == null ? BigDecimal.ZERO : balance;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance == null ? BigDecimal.ZERO : balance;
  }
}
