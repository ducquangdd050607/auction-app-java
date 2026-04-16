package com.auctionhub.server.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionLockManager {
    private final ConcurrentHashMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock acquire(UUID auctionId) {
        ReentrantLock lock = locks.computeIfAbsent(auctionId, ignored -> new ReentrantLock());
        lock.lock();
        return lock;
    }
}
