package models;

import java.time.LocalDateTime;
public class BidTransaction {
    private Bidder bidder;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount() { 
        return this.amount; 
    }
    public Bidder getBidder() { 
        return this.bidder; 
    }
    public LocalDateTime getTimestamp() { 
        return this.timestamp; 
    }
}