package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record LoginResponse(
        String id,
        String username,
        String fullName,
        String role,
        String email,
        BigDecimal walletBalance,
        boolean accStatus
) implements Serializable {}