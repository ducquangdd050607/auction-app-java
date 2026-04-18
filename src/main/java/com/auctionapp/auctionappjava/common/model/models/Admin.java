package com.auctionapp.auctionappjava.common.model.models;

public class Admin extends User {

    public Admin(String id, String name, String email, String password) {
        super(id, name, email, password);
    }

    // Khóa tài khoản người dùng
    public void banUser(User user) {
        // Kiểm tra đầu vào hợp lệ
        if (user == null) {
            System.out.println("Error: User is not existed");
            return;
        }
        
        // Không cho phép Admin tự ban chính mình
        if (user.getId().equals(this.getId())) {
            System.out.println("Error: Admin cannot ban themselves");
            return;
        }
        
        user.setActive(false);
        System.out.println("Success: User " + user.getName() + " has been banned");
    }

    // Hủy phiên đấu giá vi phạm
    public void cancelAuction(Item item) {
        // Kiểm tra đầu vào hợp lệ
        if (item == null) {
            System.out.println("Error: Item is not existed");
            return;
        }

        if (item.isAuctionEnded()) {
            System.out.println("Error: This bidding session has been out of time");
            return;
        }
        
        // Hủy bid hiện tại
        item.setIsCanceled();
        System.out.println("Success: This bidding session of " + item.getName() + " has been canceled");
    }
}