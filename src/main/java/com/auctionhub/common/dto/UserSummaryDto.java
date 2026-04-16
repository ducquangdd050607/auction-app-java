package com.auctionhub.common.dto;

import com.auctionhub.common.enums.Role;

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
