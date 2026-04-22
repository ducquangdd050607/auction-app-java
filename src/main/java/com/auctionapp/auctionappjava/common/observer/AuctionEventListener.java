package com.auctionapp.auctionappjava.common.observer;

import com.auctionapp.auctionappjava.common.dto.AuctionEventDto;

public interface AuctionEventListener {
    void onAuctionEvent(AuctionEventDto event);
}