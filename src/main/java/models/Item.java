package models;

import java.time.LocalDateTime;
public abstract class Item extends Entity {
    private double startPrice;
    private double currentPrice;
    private Customer owner;
    private LocalDateTime endTime;

    public Item(String id, String name, double startPrice, Customer owner, LocalDateTime endTime) {
        super(id, name);
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.owner = owner;
        this.endTime = endTime;
    }

    public double getCurrentPrice() { 
        return currentPrice; 
    }

    public void setCurrentPrice(double currentPrice) { 
        this.currentPrice = currentPrice; 
    }

    public LocalDateTime getEndTime() { 
        return endTime; 
    }

    public void setEndTime(LocalDateTime endTime) { 
        this.endTime = endTime; 
    }   
    
    // Kiểm tra xem sản phẩm hết hạn chưa
    public boolean isAuctionEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    // Mỗi sản phẩm có info riêng
    public abstract void printInfo();
}