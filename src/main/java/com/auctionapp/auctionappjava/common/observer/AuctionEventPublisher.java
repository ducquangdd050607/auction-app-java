package com.auctionapp.auctionappjava.common.observer;
//nơi phát sự kiện (event) cho các listener
//Một listener (Bidder, UI, AutoBid...) muốn theo dõi auction nào đó->gọi hàm này
import com.auctionapp.auctionappjava.common.dto.AuctionEventDto;
import java.util.UUID;

public interface AuctionEventPublisher {
    void subscribe(UUID auctionId, AuctionEventListener l);
    void unsubscribe(UUID auctionId, AuctionEventListener l);
    void publish(AuctionEventDto event);
}