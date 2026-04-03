package com.auctionapp.auctionappjava.models;

import java.time.LocalDateTime;
public class BidTransaction {
    private Customer bidder;
    private double amount;
    private LocalDateTime time;

    public BidTransaction(Customer bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public double getAmount() { 
        return this.amount; 
    }
    public Customer getBidder() { 
        return this.bidder; 
    }
    public LocalDateTime getTime() { 
        return this.time; 
    }
}