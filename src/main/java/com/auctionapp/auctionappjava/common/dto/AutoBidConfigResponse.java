package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record AutoBidConfigResponse(BigDecimal maxBid, BigDecimal incrementAmount, boolean enabled)
    implements Serializable {}
