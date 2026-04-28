package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record PlaceBidRequest(String auctionId, BigDecimal bidAmount) implements Serializable {}
