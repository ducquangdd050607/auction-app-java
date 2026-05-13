package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record RemoveAuctionRequest(
        String auctionId
) implements Serializable {
}
