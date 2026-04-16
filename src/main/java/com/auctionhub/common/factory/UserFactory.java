package com.auctionhub.common.factory;

import com.auctionhub.common.enums.Role;
import com.auctionhub.common.model.Admin;
import com.auctionhub.common.model.Bidder;
import com.auctionhub.common.model.Seller;
import com.auctionhub.common.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public final class UserFactory {
    private UserFactory() {
    }

    public static User create(Role role,
                              UUID id,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt,
                              String username,
                              String passwordHash,
                              String passwordSalt,
                              String fullName,
                              String email,
                              boolean active) {
        return switch (role) {
            case BIDDER -> new Bidder(id, createdAt, updatedAt, username, passwordHash, passwordSalt, fullName, email, active);
            case SELLER -> new Seller(id, createdAt, updatedAt, username, passwordHash, passwordSalt, fullName, email, active);
            case ADMIN -> new Admin(id, createdAt, updatedAt, username, passwordHash, passwordSalt, fullName, email, active);
        };
    }
}
