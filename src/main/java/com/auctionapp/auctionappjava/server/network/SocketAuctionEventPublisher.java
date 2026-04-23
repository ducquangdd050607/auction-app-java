package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.AuctionEventDto;
import com.auctionapp.auctionappjava.common.observer.*;

import java.util.*;
import java.util.concurrent.*;

public class SocketAuctionEventPublisher implements AuctionEventPublisher {
    private final ConcurrentMap<UUID, CopyOnWriteArraySet<AuctionEventListener>> listeners = new ConcurrentHashMap<>();

    @Override
    public void subscribe(UUID auctionId, AuctionEventListener l) {
        if (auctionId != null && l != null)
            listeners.computeIfAbsent(auctionId, id -> new CopyOnWriteArraySet<>()).add(l);
    }

    @Override
    public void unsubscribe(UUID auctionId, AuctionEventListener l) {
        if (auctionId == null || l == null) return;
        Set<AuctionEventListener> set = listeners.get(auctionId);
        if (set != null) {
            set.remove(l);
            if (set.isEmpty()) listeners.remove(auctionId, set);
        }
    }

    public void unsubscribeAll(AuctionEventListener l) {
        for (UUID id : new ArrayList<>(listeners.keySet())) unsubscribe(id, l);
    }

    @Override
    public void publish(AuctionEventDto event) {
        if (event == null) return;
        Set<AuctionEventListener> set = event.auctionId() == null ? Set.of() : listeners.getOrDefault(event.auctionId(), new CopyOnWriteArraySet<>());
        for (AuctionEventListener l : set) {
            try {
                l.onAuctionEvent(event);
            } catch (Exception ignored) {
            }
        }
    }
}
