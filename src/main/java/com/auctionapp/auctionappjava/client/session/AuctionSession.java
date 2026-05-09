package com.auctionapp.auctionappjava.client.session;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;

public class AuctionSession {

    private static AuctionSession instance;

    private AuctionSummaryResponse currentAuction;

    private AuctionSession() {}

    // Cấp phát chìa khóa (Thread-safe)
    public static synchronized AuctionSession getInstance() {
        if (instance == null) {
            instance = new AuctionSession();
        }
        return instance;
    }

    public AuctionSummaryResponse getCurrentAuction() {
        return currentAuction;
    }

    public void setCurrentAuction(AuctionSummaryResponse auction) {
        this.currentAuction = auction;
    }

    public void cleanAuctionSession() {
        currentAuction = null;
    }
}