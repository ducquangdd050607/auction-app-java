package com.auctionapp.auctionappjava.common.enums;

public enum EventType {
<<<<<<< HEAD
    AUCTION_UPDATED,
    BID_PLACED,
    AUCTION_EXTENDED,
    AUCTION_FINISHED,
    AUCTION_STATUS_CHANGED,
    SYSTEM_MESSAGE
}
=======
    AUCTION_UPDATED, //Thông tin auction thay đổi (title, price, time…)
    BID_PLACED, //Có người đặt giá
    AUCTION_STATUS_CHANGED,//Trạng thái auction đổi (OPEN → RUNNING → FINISHED)
    SYSTEM_MESSAGE //Thông báo hệ thống (log, cảnh báo…) (vì ko có method ở dưới nên k cần ;)
}
// các event xảy ra trong đấu giá
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
