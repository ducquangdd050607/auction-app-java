package com.auctionhub.server.network;

import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.exception.AuthException;

public class ClientSession {
    private volatile AuthUserDto currentUser;

    public void login(AuthUserDto user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public AuthUserDto currentUser() {
        return currentUser;
    }

    public AuthUserDto requireUser() {
        if (currentUser == null) {
            throw new AuthException("Bạn cần đăng nhập để sử dụng chức năng này.");
        }
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
