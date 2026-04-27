package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record LoginResponse(String username, String role) implements Serializable {}