package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.util.UUID;

public record UpdateProfileRequest(
        UUID userId,
        String fullName,
        String email
) implements Serializable {}