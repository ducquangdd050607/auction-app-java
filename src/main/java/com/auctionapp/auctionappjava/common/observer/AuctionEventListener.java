package com.auctionapp.auctionappjava.common.observer;

import com.auctionapp.auctionappjava.common.dto.ApiEnvelope;

public interface AuctionEventListener {
    void onAuctionEvent(ApiEnvelope event);
}
