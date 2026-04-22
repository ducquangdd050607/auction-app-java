package com.auctionapp.auctionappjava.common.enums;

public enum EventType {
    AUCTION_UPDATED, //Thông tin auction thay đổi (title, price, time…)
    BID_PLACED, //Có người đặt giá
    AUCTION_STATUS_CHANGED,//Trạng thái auction đổi (OPEN → RUNNING → FINISHED)
    SYSTEM_MESSAGE //Thông báo hệ thống (log, cảnh báo…) (vì ko có method ở dưới nên k cần ;)
}
// các event xảy ra trong đấu giá