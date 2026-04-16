package com.auctionhub.common.observer;

import com.auctionhub.common.dto.ApiEnvelope;

public interface AuctionEventListener {
    void onAuctionEvent(ApiEnvelope event);
}
