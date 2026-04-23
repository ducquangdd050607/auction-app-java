package com.auctionapp.auctionappjava.server.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class AuctionLockManager {
    private final ConcurrentMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();
    public <T> T executeWithLock(UUID auctionId, Supplier<T> supplier) {
        ReentrantLock lock = locks.computeIfAbsent(auctionId, ignored -> new ReentrantLock(true));
        lock.lock();
        try { return supplier.get(); }
        finally {
            lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) locks.remove(auctionId, lock);
        }
    }
    public void executeWithLock(UUID auctionId, Runnable runnable) {
        executeWithLock(auctionId, () -> { runnable.run(); return null; });
    }
}
