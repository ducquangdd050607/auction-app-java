package com.auctionapp.auctionappjava.common.observer;

import com.auctionapp.auctionappjava.common.dto.AuctionEventDto;
import java.util.UUID;

public interface AuctionEventPublisher {
    void subscribe(UUID auctionId, AuctionEventListener l);
    void unsubscribe(UUID auctionId, AuctionEventListener l);
    void publish(AuctionEventDto event);
}