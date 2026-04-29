package com.auctionapp.auctionappjava.common.dto;
import java.io.Serializable;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String email,
        String role
) implements Serializable {}