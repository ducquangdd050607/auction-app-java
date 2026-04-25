package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;

    protected User() {
        super();
        this.active = true;
    }

    protected User(UUID id,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt,
                   String username,
                   String passwordHash,
                   String passwordSalt,
                   String fullName,
                   String email,
                   Role role,
                   boolean active) {
        super(id, createdAt, updatedAt);
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
