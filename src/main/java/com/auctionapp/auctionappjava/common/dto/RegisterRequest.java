package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

import com.auctionapp.auctionappjava.common.enums.Role;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String email,
        Role role
) implements Serializable {}