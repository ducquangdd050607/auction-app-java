package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record RealtimeUpdateResponse(String auctionId, double newPrice, String topBidderName)
    implements Serializable {}
