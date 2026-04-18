package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.Role;

import java.io.Serializable;
import java.util.UUID;

public record AuthUserDto(UUID id, String username, String fullName, String email, Role role) implements Serializable {
}
