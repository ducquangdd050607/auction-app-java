package com.auctionapp.auctionappjava.server.realtime;

import com.auctionapp.auctionappjava.common.dto.AuctionRealtimeEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class AuctionRealtimeHub {
    private static final AuctionRealtimeHub INSTANCE = new AuctionRealtimeHub();

    private final ConcurrentHashMap<UUID, CopyOnWriteArraySet<ClientConnection>> subscribers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ClientConnection, CopyOnWriteArraySet<UUID>> subscriptionsByClient = new ConcurrentHashMap<>();

    private AuctionRealtimeHub() {}

    public static AuctionRealtimeHub getInstance() {
        return INSTANCE;
    }

    public void subscribe(UUID auctionId, ClientConnection connection) {
        if (auctionId == null || connection == null) return;
        subscribers.computeIfAbsent(auctionId, key -> new CopyOnWriteArraySet<>()).add(connection);
        subscriptionsByClient.computeIfAbsent(connection, key -> new CopyOnWriteArraySet<>()).add(auctionId);
    }

    public void unsubscribe(UUID auctionId, ClientConnection connection) {
        if (auctionId == null || connection == null) return;
        Set<ClientConnection> auctionSubscribers = subscribers.get(auctionId);
        if (auctionSubscribers != null) {
            auctionSubscribers.remove(connection);
            if (auctionSubscribers.isEmpty()) {
                subscribers.remove(auctionId);
            }
        }
        Set<UUID> clientSubscriptions = subscriptionsByClient.get(connection);
        if (clientSubscriptions != null) {
            clientSubscriptions.remove(auctionId);
            if (clientSubscriptions.isEmpty()) {
                subscriptionsByClient.remove(connection);
            }
        }
    }

    public void unsubscribeAll(ClientConnection connection) {
        if (connection == null) return;
        Set<UUID> auctionIds = subscriptionsByClient.remove(connection);
        if (auctionIds == null) return;
        for (UUID auctionId : auctionIds) {
            Set<ClientConnection> auctionSubscribers = subscribers.get(auctionId);
            if (auctionSubscribers != null) {
                auctionSubscribers.remove(connection);
                if (auctionSubscribers.isEmpty()) {
                    subscribers.remove(auctionId);
                }
            }
        }
    }

    public void broadcast(UUID auctionId, AuctionRealtimeEvent event) {
        if (auctionId == null || event == null) return;
        Set<ClientConnection> auctionSubscribers = subscribers.get(auctionId);
        if (auctionSubscribers == null || auctionSubscribers.isEmpty()) return;

        for (ClientConnection connection : auctionSubscribers) {
            try {
                connection.sendEvent(event);
            } catch (Exception e) {
                unsubscribe(auctionId, connection);
            }
        }
    }
}
