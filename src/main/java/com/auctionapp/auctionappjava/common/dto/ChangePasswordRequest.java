package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.util.UUID;

public record ChangePasswordRequest(
        UUID userId,
        String oldPassword,
        String newPassword
) implements Serializable {}