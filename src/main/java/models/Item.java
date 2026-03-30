package models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public abstract class Item extends Entity {
    private double startPrice;
    private double currentPrice;
    private Customer owner;
    private LocalDateTime endTime;
    private String imagePath; // Lưu đường dẫn ảnh
    private String description; // Mô tả chi tiết sản phẩm
    private List<BidTransaction> bidHistory; // Lịch sử đặt bid
    private boolean isCanceled = false; // Kiểm tra item có bị buộc phải hủy bid hay không

    public Item(String id, String name, double startPrice, Customer owner, LocalDateTime endTime) {
        super(id, name);
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.owner = owner;
        this.endTime = endTime;
        this.bidHistory = new ArrayList<>();
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
    
    // Nếu cần sẽ hủy bid tại đây
    public void setIsCanceled() { 
        this.isCanceled = true; 
    }  
    
    // Kiểm tra xem sản phẩm hết hạn chưa
    public boolean isAuctionEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    // Mỗi sản phẩm có info riêng
    public abstract void printInfo();
    
    // Quản lý giá đặt bid
    public boolean placeBid(Customer bidder, double bidAmount) {
    	// Kiểm tra bid hết hạn/bid giá thấp hơn giá cao nhất/balance k đủ tiền bid
        if ((isAuctionEnded()) || (bidAmount <= currentPrice) || (bidder.getWallet().getBalance() < bidAmount)) return false;

        // Cập nhật giá và ghi vào lịch sử đặt bid
        this.currentPrice = bidAmount;
        this.bidHistory.add(new BidTransaction(bidder, bidAmount));
        return true;
    }
}