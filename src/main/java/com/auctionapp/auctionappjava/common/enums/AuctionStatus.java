package com.auctionapp.auctionappjava.common.enums;

public enum AuctionStatus {
    OPEN,
    RUNNING,
    FINISHED,
    PAID,
    CANCELED;

    public boolean isClosedForBidding() {
        return this == FINISHED || this == PAID || this == CANCELED;
    }
}
