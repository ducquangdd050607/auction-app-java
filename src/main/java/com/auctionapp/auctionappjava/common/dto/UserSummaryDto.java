package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.Role;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserSummaryDto(UUID id,
                             String username,
                             String fullName,
                             String email,
                             Role role,
                             boolean active,
                             LocalDateTime createdAt) implements Serializable {
}
