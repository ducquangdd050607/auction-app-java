package com.auctionapp.auctionappjava.server.service.postauction;

import com.auctionapp.auctionappjava.common.model.Auction;

import java.time.LocalDateTime;

public interface DeadlinePolicy {
    LocalDateTime calculateDeadline(Auction auction, LocalDateTime now);
}
