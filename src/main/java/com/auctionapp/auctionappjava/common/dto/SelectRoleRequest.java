package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.util.UUID;

import com.auctionapp.auctionappjava.common.enums.Role;

public record SelectRoleRequest(
        UUID userId,
        Role role,
        String adminKey
) implements Serializable {}