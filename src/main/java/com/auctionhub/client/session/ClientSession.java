package com.auctionhub.client.session;

import com.auctionhub.common.dto.AuthUserDto;

public final class ClientSession {
    private static final ClientSession INSTANCE = new ClientSession();
    private volatile AuthUserDto currentUser;

    private ClientSession() {
    }

    public static ClientSession getInstance() {
        return INSTANCE;
    }

    public void login(AuthUserDto user) {
        this.currentUser = user;
    }

    public void clear() {
        this.currentUser = null;
    }

    public AuthUserDto getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
