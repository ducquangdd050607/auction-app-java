package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record LoginRequest(String username, String password) implements Serializable {
}
