package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record BidManagerAndHistoryRequest(
        String userId

) implements Serializable {
}
// 2-in-1, huh