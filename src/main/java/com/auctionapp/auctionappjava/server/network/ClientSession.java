package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.AuthUserDto;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSession {
    private volatile AuthUserDto currentUser;
    private final Set<UUID> subscribedAuctions = ConcurrentHashMap.newKeySet();

    public AuthUserDto getCurrentUser() {
        return currentUser;
    }

    public UUID getCurrentUserId() {
        return currentUser == null ? null : currentUser.id();
    }

    public void login(AuthUserDto user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
        subscribedAuctions.clear();
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public void subscribe(UUID auctionId) {
        subscribedAuctions.add(auctionId);
    }

    public void unsubscribe(UUID auctionId) {
        subscribedAuctions.remove(auctionId);
    }

    public Set<UUID> getSubscribedAuctions() {
        return Set.copyOf(subscribedAuctions);
    }
}
