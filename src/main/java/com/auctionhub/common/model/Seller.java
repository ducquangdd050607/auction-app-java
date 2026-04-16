package com.auctionhub.common.model;

import com.auctionhub.common.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class Seller extends User {
    public Seller() {
        super();
        setRole(Role.SELLER);
    }

    public Seller(UUID id,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt,
                  String username,
                  String passwordHash,
                  String passwordSalt,
                  String fullName,
                  String email,
                  boolean active) {
        super(id, createdAt, updatedAt, username, passwordHash, passwordSalt, fullName, email, Role.SELLER, active);
    }
}
