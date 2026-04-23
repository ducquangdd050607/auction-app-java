package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.util.UUID;

public record UserIdRequest(UUID userId) implements Serializable {
}
