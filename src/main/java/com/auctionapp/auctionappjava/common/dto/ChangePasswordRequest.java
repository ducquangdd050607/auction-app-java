package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record ChangePasswordRequest(
        String userId,
        String newPassword) implements Serializable {
}
