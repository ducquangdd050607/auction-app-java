package com.auctionhub.server.network;

import com.auctionhub.common.dto.ApiEnvelope;
import com.auctionhub.common.observer.AuctionEventListener;
import com.auctionhub.common.observer.AuctionEventPublisher;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SocketAuctionEventPublisher implements AuctionEventPublisher {
    private final Map<UUID, Set<AuctionEventListener>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(UUID auctionId, AuctionEventListener listener) {
        subscribers.computeIfAbsent(auctionId, ignored -> new CopyOnWriteArraySet<>()).add(listener);
    }

    @Override
    public void unsubscribe(UUID auctionId, AuctionEventListener listener) {
        Set<AuctionEventListener> listeners = subscribers.get(auctionId);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                subscribers.remove(auctionId);
            }
        }
    }

    @Override
    public void unsubscribeAll(AuctionEventListener listener) {
        subscribers.forEach((auctionId, listeners) -> listeners.remove(listener));
    }

    @Override
    public void publish(UUID auctionId, ApiEnvelope event) {
        subscribers.getOrDefault(auctionId, Set.of()).forEach(listener -> listener.onAuctionEvent(event));
    }
}
