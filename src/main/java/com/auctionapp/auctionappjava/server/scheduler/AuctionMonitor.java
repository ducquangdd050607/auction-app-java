package com.auctionapp.auctionappjava.server.scheduler;

import com.auctionapp.auctionappjava.server.service.AuctionLifecycleService;

import java.io.Closeable;
import java.util.concurrent.*;

public class AuctionMonitor implements Closeable {
    private final AuctionLifecycleService lifecycleService;
    private final long intervalMs;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "auction-monitor");
        t.setDaemon(true);
        return t;
    });

    public AuctionMonitor(AuctionLifecycleService lifecycleService, long intervalMs) {
        this.lifecycleService = lifecycleService;
        this.intervalMs = intervalMs;
    }

    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            try {
                lifecycleService.activateDueAuctions();
                lifecycleService.closeExpiredAuctions();
            } catch (Exception e) {
                System.err.println("AuctionMonitor error: " + e.getMessage());
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
