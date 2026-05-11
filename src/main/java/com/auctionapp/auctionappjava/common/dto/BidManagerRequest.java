package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record BidManagerRequest(
        String sellerId

) implements Serializable {
}
