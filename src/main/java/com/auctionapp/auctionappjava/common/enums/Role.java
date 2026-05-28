package com.auctionapp.auctionappjava.common.enums;

public enum Role {
  BIDDER,
  SELLER,
  ADMIN;

  public boolean isAdmin() {
    return this == ADMIN;
  }

  public boolean isSeller() {
    return this == SELLER;
  }

  public boolean isBidder() {
    return this == BIDDER;
  }
}
