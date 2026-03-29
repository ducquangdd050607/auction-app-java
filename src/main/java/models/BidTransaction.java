package models;

import java.time.LocalDateTime;
public class BidTransaction {
    private Customer bidder;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction(Customer bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount() { 
        return this.amount; 
    }
    public Customer getBidder() { 
        return this.bidder; 
    }
    public LocalDateTime getTimestamp() { 
        return this.timestamp; 
    }
}