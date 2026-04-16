package com.auctionhub.common.dto;

import com.auctionhub.common.enums.Role;

import java.io.Serializable;
import java.util.UUID;

public record AuthUserDto(UUID id, String username, String fullName, String email, Role role) implements Serializable {
}
