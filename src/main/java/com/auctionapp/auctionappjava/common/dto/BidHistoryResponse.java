package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record BidHistoryResponse(String bidderName, BigDecimal amount, long bidTimeMillis) implements Serializable {}