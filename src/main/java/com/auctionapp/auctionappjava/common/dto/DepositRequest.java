package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record DepositRequest(String userId, BigDecimal amount) implements Serializable {}
