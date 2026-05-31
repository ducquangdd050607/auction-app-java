package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.util.UUID;

public record AutoBidRequest(UUID auctionId, UUID bidderId) implements Serializable {}
