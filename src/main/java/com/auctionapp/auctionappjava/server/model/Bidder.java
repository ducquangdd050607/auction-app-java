package com.auctionapp.auctionappjava.server.model;

import com.auctionapp.auctionappjava.common.enums.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public class Bidder extends User {
  public Bidder() {
    super();
    setRole(Role.BIDDER);
  }

  public Bidder(
      UUID id,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      String username,
      String passwordHash,
      String passwordSalt,
      String fullName,
      String email,
      boolean active) {
    super(
        id,
        createdAt,
        updatedAt,
        username,
        passwordHash,
        passwordSalt,
        fullName,
        email,
        Role.BIDDER,
        active);
  }
}
