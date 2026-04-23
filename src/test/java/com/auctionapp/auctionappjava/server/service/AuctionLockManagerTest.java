package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AuctionLockManagerTest {

    @Test
    void executeWithLockSerializesWorkForSameAuction() throws Exception {
        AuctionLockManager lockManager = new AuctionLockManager();
        UUID auctionId = UUID.randomUUID();
        AtomicInteger activeWorkers = new AtomicInteger();
        AtomicInteger maxActiveWorkers = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            tasks.add(() -> lockManager.executeWithLock(auctionId, () -> {
                int active = activeWorkers.incrementAndGet();
                maxActiveWorkers.updateAndGet(previous -> Math.max(previous, active));
                try {
                    Thread.sleep(10);
                } finally {
                    activeWorkers.decrementAndGet();
                }
                return null;
            }));
        }

        executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        assertEquals(1, maxActiveWorkers.get());
    }
}
