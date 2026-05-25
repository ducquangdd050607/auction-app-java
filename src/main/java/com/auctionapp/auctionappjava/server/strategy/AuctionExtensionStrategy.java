package com.auctionapp.auctionappjava.server.strategy;

import com.auctionapp.auctionappjava.server.model.Auction;

import java.time.LocalDateTime;
// là 1 interface định nghĩa chiến lược gia hạn phiên đấu giá (auction extension)
public interface AuctionExtensionStrategy {
    //có gia hạn hay không
    boolean shouldExtend(Auction auction, LocalDateTime bidTime);

    LocalDateTime extendTo(Auction auction, LocalDateTime bidTime);
    // extendto là gia hạn tới thời điểm nào
}
