package com.auctionapp.auctionappjava.common.observer;
//Định nghĩa những object nào muốn lắng nghe sự kiện từ Auction
import com.auctionapp.auctionappjava.common.dto.AuctionEventDto;

public interface AuctionEventListener {
    void onAuctionEvent(AuctionEventDto event);
}