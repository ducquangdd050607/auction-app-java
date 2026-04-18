package com.auctionapp.auctionappjava.common.observer;

import com.auctionapp.auctionappjava.common.dto.ApiEnvelope;

import java.util.UUID;

public interface AuctionEventPublisher {
    void subscribe(UUID auctionId, AuctionEventListener listener);

    void unsubscribe(UUID auctionId, AuctionEventListener listener);

    void unsubscribeAll(AuctionEventListener listener);

    void publish(UUID auctionId, ApiEnvelope event);
}
