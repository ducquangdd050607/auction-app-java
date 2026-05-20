package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record RemoveAuctionRequest(
        String userId,
        String auctionId
) implements Serializable {
}
