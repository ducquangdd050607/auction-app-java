package com.auctionapp.auctionappjava.client.session;

import com.auctionapp.auctionappjava.common.dto.LoginResponse;

public class UserSession {
    // Biến instance duy nhất của class này
    private static UserSession instance;

    // Thuộc tính để lưu trữ bưu kiện DTO từ Server
    private LoginResponse currentUser;

    // Khóa Constructor lại để không ai được quyền tạo bằng từ khóa 'new'
    private UserSession() {}

    // Cấp phát chìa khóa (Thread-safe)
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Lấy thông tin user hiện tại
    public LoginResponse getCurrentUser() {
        return currentUser;
    }

    // Lưu thông tin user lúc đăng nhập
    public void setCurrentUser(LoginResponse user) {
        this.currentUser = user;
    }

    // Xóa sạch thông tin lúc đăng xuất
    public void cleanUserSession() {
        currentUser = null;
    }
}