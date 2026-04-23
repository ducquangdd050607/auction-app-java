package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import com.auctionapp.auctionappjava.common.enums.Role;

public record UserSummaryDto(
        UUID id,
        String username,
        String fullName,
        String email,
        Role role,
        boolean active,
        BigDecimal walletBalance,
        long bidsCount
) implements Serializable {}