package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.Role;

import java.io.Serializable;

public record RegisterRequest(String username,
                              String password,
                              String confirmPassword,
                              String fullName,
                              String email,
                              Role role) implements Serializable {
}
