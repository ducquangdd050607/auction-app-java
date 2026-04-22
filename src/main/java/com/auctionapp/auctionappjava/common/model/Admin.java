package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;
//Admin is a User
public class Admin extends User {
    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(UUID id,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt,
                 String username,
                 String passwordHash,
                 String passwordSalt,//Salt = chuỗi random thêm vào password trước khi hash
                 String fullName,
                 String email,
                 boolean active) {
        super(id, createdAt, updatedAt, username, passwordHash, passwordSalt, fullName, email, Role.ADMIN, active);
    }
}
